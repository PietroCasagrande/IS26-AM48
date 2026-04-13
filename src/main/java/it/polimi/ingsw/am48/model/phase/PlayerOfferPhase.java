package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.BuildingCardPickedDelta;
import it.polimi.ingsw.am48.model.delta.CharacterCardPickedDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerOfferPhaseSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlayerOfferPhase implements GamePhase {
    private final List<Player> actionOrder;
    private int currIdx;
    private int picksFromUp;
    private int picksFromDown;
    private boolean extraPickActive;     // per l'edificio extra pick
    private Player extraPickPlayer;      // chi ha l'edificio

    public PlayerOfferPhase(List<Player> actionOrder, Game  game) {
        this.actionOrder = new ArrayList<Player>(actionOrder);
        currIdx = 0;
        picksFromUp = 0;
        picksFromDown = 0;
        extraPickActive = false;
        extraPickPlayer = null;

        OfferCard firstOffer = game.getBoard().findTrackPosition(actionOrder.getFirst());

        if(firstOffer.getLetterId() == 'A'){
            Player firstPlayer = actionOrder.getFirst();
            firstPlayer.updateFood(firstOffer.getFoodBonus());
            handleTotemReturn(game, firstPlayer);
            currIdx++;
        }
    }

    @Override
    public List<GameDelta> takeCard(Game game, Player player, String cardId) {
        List<GameDelta> deltas = new ArrayList<>();

        // 1. Validazione turno
        if (extraPickActive) {
            // Siamo nella fase extra: solo il player con l'edificio può giocare
            if (!player.equals(extraPickPlayer)) {
                throw new InvalidActionException("Non è il tuo turno.");
            }
            // Può prendere solo dalla fila superiore
            if (!game.getBoard().isCardTop(cardId)) {
                throw new InvalidActionException("Puoi prendere solo dalla fila superiore.");
            }
        } else {
            // Turno normale
            if (!actionOrder.get(currIdx).equals(player)) {
                throw new InvalidActionException("Non è il tuo turno.");
            }
        }

        // 2. Trova la tessera offerta del giocatore corrente
        OfferCard currentOffer = game.getBoard().findTrackPosition(player);

        // 3. Valida il pick (solo nel turno normale, non nel extra)
        if (!extraPickActive) {
            validatePick(game.getBoard(), cardId, currentOffer);
        }

        // 4. Prendi la carta dal board (acquire + rimuovi da showed)
        Card selectedCard = game.getBoard().takeCard(player, cardId);

        // 5. Aggiorna contatori
        if (!extraPickActive) {
            if (game.getBoard().isCardTop(cardId)) {
                picksFromUp++;
            } else {
                picksFromDown++;
            }
        }

        // 6. Registra la strategy della carta al notificator
        if (selectedCard.getStrategy() != null) {
            game.getPlayerContext().setCurrPlayer(player);
            selectedCard.getStrategy().registerTo(
                    game.getNotificatorCenter(), game.getPlayerContext());
            // Attiva OnPick
            game.getNotificatorCenter().getPickNotificator()
                    .notify(game.getPlayerContext());
        }

        // 7. Costruisci il delta
        deltas.add(buildCardDelta(player, selectedCard, game));

        // 8. Se siamo nel extra pick, abbiamo finito
        if (extraPickActive) {
//            extraPickActive = false;
//            extraPickPlayer = null;
            // Transizione a EndTurnPhase
            EndTurnPhase endPhase = new EndTurnPhase();
            game.setPhase(endPhase);
            deltas.addAll(endPhase.endTurn(game));
            return deltas;
        }

        // 9. Controlla se il giocatore ha finito i suoi pick
        if (picksFromUp + picksFromDown >= currentOffer.getTotalPicks()) {
            // Totem torna sulla tessera ordine di turno
            handleTotemReturn(game, player);

            // Reset per il prossimo giocatore
            picksFromUp = 0;
            picksFromDown = 0;
            currIdx++;
        }

        // 10. Controlla se il round è finito
        if (currIdx == actionOrder.size()) {
            // Controlla se qualcuno ha l'edificio "extra pick"
            // OnEndOfferPhaseNotificator controlla e setta il extra
            checkExtraPick(game);

            if (!extraPickActive) {
                // Nessun extra: transizione a EndTurnPhase
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.addAll(endPhase.endTurn(game));
            }
            // Se extraPickActive, la fase resta PlayerOfferPhase
            // e aspetta il takeCard del extra player
        }

        return deltas;
    }

    private void validatePick(Board board, String cardId, OfferCard offer) {
        boolean isTop = board.isCardTop(cardId);
        boolean isDown = board.isCardDown(cardId);

        if (!isTop && !isDown) {
            throw new InvalidActionException("La carta non è sul tabellone.");
        }
        if (isTop && picksFromUp >= offer.getNumUp()) {
            throw new InvalidActionException("Hai già pescato il massimo dalla fila superiore.");
        }
        if (isDown && picksFromDown >= offer.getNumDown()) {
            throw new InvalidActionException("Hai già pescato il massimo dalla fila inferiore.");
        }
    }

    private void handleTotemReturn(Game game, Player player) {
        // Riporta totem sulla tessera ordine di turno
        game.getBoard().returnTotem(player);
        // Attiva OnTotemReturned (edificio cibo extra)
        game.getPlayerContext().setCurrPlayer(player);
        game.getNotificatorCenter().getTotemReturnedNotificator()
                .notify(game.getPlayerContext());
    }

    private void checkExtraPick(Game game) {
        // L'edificio "extra pick" è registrato su OnEndOfferPhaseNotificator
        // Controlliamo se c'è un listener
        // Se sì, settiamo extraPickActive ed extraPickPlayer
        // Il notify dell'OnEndOfferPhaseNotificator non esegue il pick,
        // setta solo un flag — il pick vero lo fa il player col prossimo takeCard
    }

    private GameDelta buildCardDelta(Player player, Card card, Game game) {
        if (card instanceof BuildingCard) {
            List<String> updatedUpperBuildingsIds = game.getBoard().getBuildingShowed().getUpperList().stream().map(Card::getCardId).toList();
            List<String> updatedLowerBuildingsIds = game.getBoard().getBuildingShowed().getLowerList().stream().map(Card::getCardId).toList();

            return new BuildingCardPickedDelta(player.getNickname(), card.getCardId(),updatedUpperBuildingsIds, updatedLowerBuildingsIds);
        }
        List<String> updatedUpperTribeIds = game.getBoard().getTribeShowed().getUpperList().stream().map(Card::getCardId).toList();
        List<String> updatedLowerTribeIds = game.getBoard().getTribeShowed().getLowerList().stream().map(Card::getCardId).toList();

        return new CharacterCardPickedDelta(player.getNickname(), card.getCardId(),updatedUpperTribeIds, updatedLowerTribeIds);
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        List<String> orderNicknames = actionOrder.stream()
                .map(Player::getNickname)
                .toList();

        return new PlayerOfferPhaseSnapshot(orderNicknames, currIdx);
    }
}
