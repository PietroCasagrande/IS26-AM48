package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.delta.*;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerOfferPhaseSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerOfferPhase implements GamePhase {
    private final List<Player> actionOrder;
    private int currIdx;
    private int picksFromUp;
    private int picksFromDown;
    private boolean extraPickActive;// per l'edificio extra pick
    private Player extraPickPlayer;      // chi ha l'edificio
    private boolean totemReturned;
    private boolean skipAvailable;

    public PlayerOfferPhase(List<Player> actionOrder) {
        this.actionOrder = new ArrayList<>(actionOrder);
        currIdx = 0;
        picksFromUp = 0;
        picksFromDown = 0;
        extraPickActive = false;
        extraPickPlayer = null;
        totemReturned = false;
        skipAvailable = false;
    }

    // costruttore utilizzato in GamePhase.fromSnapshot()
    public PlayerOfferPhase(List<Player> actionOrder, int currIdx, int picksFromUp,
                            int picksFromDown, boolean extraPickActive,
                            Player extraPickPlayer, boolean totemReturned) {
        this.actionOrder = new ArrayList<>(actionOrder);
        this.currIdx = currIdx;
        this.picksFromUp = picksFromUp;
        this.picksFromDown = picksFromDown;
        this.extraPickActive = extraPickActive;
        this.extraPickPlayer = extraPickPlayer;
        this.totemReturned = totemReturned;
    }

    // controlla se c'è qualcuno sulla tessera A e, in caso affermativo, assegna cibo, ritorna totem e restituisce nuovo delta apposito
    public Optional<GameDelta> setup(Game game){
        OfferCard firstOffer = game.getBoard().findTrackPosition(actionOrder.getFirst());

        if(firstOffer.getLetterId() == 'A'){
            Player firstPlayer = actionOrder.getFirst();
            firstPlayer.updateFood(firstOffer.getFoodBonus());
            handleTotemReturn(game, firstOffer.returnTotem().orElse(null));
            currIdx++;

            return Optional.of(new OfferCardADelta(firstPlayer.getNickname(), firstPlayer.getFood()));
        }

        return Optional.empty();
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
            else if(cardId.equals("skip")){
                extraPickActive = false;
                extraPickPlayer = null;
                // Transizione a EndTurnPhase
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.addAll(endPhase.endTurn(game));
                return deltas;
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

        // Sets current player in PlayerContext
        game.getPlayerContext().setCurrPlayer(player);

        // 2. Trova la tessera offerta del giocatore corrente
        OfferCard currentOffer = null;

        // 3. Valida il pick (solo nel turno normale, non nel extra)
        if (!extraPickActive) {
             currentOffer = game.getBoard().findTrackPosition(player);
            validatePick(game.getBoard(), cardId, currentOffer);
        }

        Card selectedCard = null;

        if(!skipAvailable){

            // Determina la posizione PRIMA di prendere la carta (serve dopo per il counter)
            boolean isTop = !extraPickActive && game.getBoard().isCardTop(cardId);

            // Prende la carta dal board - se lancia eccezione, i contatori non vengono toccati
            selectedCard = game.getBoard().takeCard(game.getPlayerContext(), cardId);

            // Aggiorna contatori solamente dopo che takeCard è andato a buon fine
            if (!extraPickActive) {
                if (isTop) {
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

            // 7. Se siamo nel extra pick, abbiamo finito
            if (extraPickActive) {
                extraPickActive = false;
                extraPickPlayer = null;
                // Transizione a EndTurnPhase
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "END_TURN"));  // se siamo all'extraPick dobbiamo costruire il delta, dato che non arriviamo a quello del punto 9
                deltas.addAll(endPhase.endTurn(game));
                return deltas;
            }
        }

        // 8. Controlla se il giocatore ha finito i suoi pick
        if (picksFromUp + picksFromDown >= currentOffer.getTotalPicks() || skipAvailable) {
            // Totem torna sulla tessera ordine di turno
            handleTotemReturn(game, currentOffer.returnTotem().get());
            totemReturned = true;

            // Reset per il prossimo giocatore
            picksFromUp = 0;
            picksFromDown = 0;
            currIdx++;
        }

        // dobbiamo costruire delta in ogni singolo ramo degli IF perchè dipende dalla fase che si ha al momento della chiamata

        // 9. Controlla se il round è finito
        if (currIdx >= actionOrder.size()) {    // non è size()-1 perchè c'è stato currIdx++ oltre l'ultimo player
            // Controlla se qualcuno ha l'edificio "extra pick"
            // OnEndOfferPhaseNotificator controlla e setta il extra
            checkExtraPick(game);

            if (!extraPickActive) {
                // Nessun extra: transizione a EndTurnPhase
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "END_TURN"));
                deltas.addAll(endPhase.endTurn(game));
            }
            // Se extraPickActive, la fase resta PlayerOfferPhase
            // e aspetta il takeCard del extra player
            else deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "PLAYER_OFFER"));
        }
        else deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "PLAYER_OFFER"));

        totemReturned = false;  // rimettiamo a false per prossimo player (in caso il curr l'avesse aggiornato)
        skipAvailable = false;

        return deltas;
    }

    private void validatePick(Board board, String cardId, OfferCard offer) {

        if(cardId.equals("skip")){
            if(picksFromUp < offer.getNumUp()){
                List<Card> tribeUpper = board.getTribeShowed().getUpperList();
                int numCharUp = (int) tribeUpper.stream()
                        .filter(c -> c instanceof CharacterCard)
                        .count();
                if(numCharUp == 0) {
                    skipAvailable = true;
                    return;
                }
                else{
                    throw new InvalidActionException("Devi pescare una carta dalla fila superiore");
                }
            }
            else if(picksFromDown < offer.getNumDown()){
                List<Card> tribeLower = board.getTribeShowed().getLowerList();
                int numCharDown = (int) tribeLower.stream()
                        .filter(c -> c instanceof CharacterCard)
                        .count();
                if(numCharDown == 0) {
                    skipAvailable = true;
                    return;
                }
                else{
                    throw new InvalidActionException("Devi pescare una carta dalla fila inferiore");
                }
            }
        }

        boolean isTop = board.isCardTop(cardId);
        boolean isDown = board.isCardDown(cardId);

        if (!isTop && !isDown) {
            throw new InvalidActionException("La carta non è sul tabellone.");
        }
        if (isTop && offer.getNumUp() == 0)
            throw new InvalidActionException("Non puoi pescare dalla fila superiore");

        if (isDown && offer.getNumDown() == 0)
            throw new InvalidActionException("Non puoi pescare dalla fila inferiore");

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
        // Notifica OnEndOfferPhase — questa chiamata attiva ExtraPickStrategy
        // che setta deservesExtraPick = true sul player che ha l'edificio
        /*game.getNotificatorCenter().getEndOfferPhaseNotificator()
                .notify(game.getPlayerContext());*/
        // L'edificio "extra pick" è registrato su OnEndOfferPhaseNotificator
        // Controlliamo se c'è un listener
        // Se sì, settiamo extraPickActive ed extraPickPlayer
        // La notify di OnEndOfferPhaseNotificator non esegue il pick,
        // setta solo un flag — il pick vero lo fa il player col prossimo takeCard
        game.getPlayerContext().getPlayers().stream()
                .filter(Player::deservesExtraPick)
                .findFirst()
                .ifPresent(p -> {
                    extraPickActive = true;
                    extraPickPlayer = p;
                });
    }

    private GameDelta buildCardDelta(Player player, Card card, Game game, boolean totemReturned, String phaseName) {
        if(card == null){
            return new SkipDelta(player.getNickname(), totemReturned, phaseName);
        }
        else if (card instanceof BuildingCard) {
            List<String> updatedUpperBuildingsIds = game.getBoard().getBuildingShowed().getUpperList().stream().map(Card::getCardId).toList();
            List<String> updatedLowerBuildingsIds = game.getBoard().getBuildingShowed().getLowerList().stream().map(Card::getCardId).toList();

            return new BuildingCardPickedDelta(player.getNickname(), card.getCardId(),updatedUpperBuildingsIds, updatedLowerBuildingsIds, player.getFood(), player.getPoints(), totemReturned, phaseName);
        }
        List<String> updatedUpperTribeIds = game.getBoard().getTribeShowed().getUpperList().stream().map(Card::getCardId).toList();
        List<String> updatedLowerTribeIds = game.getBoard().getTribeShowed().getLowerList().stream().map(Card::getCardId).toList();

        return new CharacterCardPickedDelta(player.getNickname(), card.getCardId(),updatedUpperTribeIds, updatedLowerTribeIds, player.getFood(), player.getPoints(), totemReturned, phaseName);
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        List<String> orderNicknames = actionOrder.stream()
                .map(Player::getNickname)
                .toList();

        return new PlayerOfferPhaseSnapshot(orderNicknames, currIdx, picksFromUp, picksFromDown, extraPickActive, extraPickPlayer != null ? extraPickPlayer.getNickname() : null, totemReturned);
    }
}
