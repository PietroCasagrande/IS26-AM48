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

/**
 * Represents the main gameplay phase where players acquire cards from the board.
 * <p>
 * This phase is highly stateful, tracking exactly whose turn it is, how many cards
 * they have picked from each row (upper or lower), and whether any special building
 * effects (like the "Extra Pick" ability) are currently altering the standard flow.
 */
public class PlayerOfferPhase implements GamePhase {
    private final List<Player> actionOrder;
    private int currIdx;
    private int picksFromUp;
    private int picksFromDown;
    private boolean extraPickActive;
    private Player extraPickPlayer;
    private boolean totemReturned;
    private boolean skipAvailable;

    /**
     * Initializes a new offer phase with a predetermined playing order.
     *
     * @param actionOrder the sorted list of players defining the turn sequence
     */
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

    /**
     * Reconstructs the phase from a saved state, restoring all internal counters and flags.
     */
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

    /**
     * Executes preliminary phase setup, specifically handling the "A" track slot bonus.
     * <p>
     * If the first player placed their totem on the "A" slot, they immediately receive
     * food, return their totem, and their turn ends before any card picking begins.
     *
     * @param game the current game instance
     * @return an {@code Optional} containing the resulting network delta, if the bonus was triggered
     */
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


    /**
     * Handles the core logic of a player attempting to take a card from the board.
     * <p>
     * This method orchestrates the validation of the turn, enforces the drawing limits
     * dictated by the player's position on the offer track, triggers card abilities
     * via the Notificator Center, and determines whether the turn or the entire phase
     * should advance. Moreover, handles extra pick building logic and skip pick logic.
     *
     * @param game   the main game instance
     * @param player the player attempting the action
     * @param cardId the unique identifier of the target card, or "skip" if the player chooses to pass
     * @return a list of network deltas representing the state changes caused by the action
     * @throws InvalidActionException if it is not the player's turn or if the pick violates the rules
     */
    @Override
    public List<GameDelta> takeCard(Game game, Player player, String cardId) {
        List<GameDelta> deltas = new ArrayList<>();

        // Turn validation
        if (extraPickActive) {
            // Only the player with extra pick can play
            if (!player.equals(extraPickPlayer)) {
                throw new InvalidActionException("Not your turn.");
            }
            else if(cardId.equals("skip")){
                extraPickActive = false;
                extraPickPlayer = null;
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.addAll(endPhase.endTurn(game));
                return deltas;
            }
            // Extra card can be taken only from upper row
            if (!game.getBoard().isCardTop(cardId)) {
                throw new InvalidActionException("You can only take from upper row.");
            }
        } else {
            // Regular turn: check if it's the current player's turn
            if (!actionOrder.get(currIdx).equals(player)) {
                throw new InvalidActionException("Not your turn.");
            }
        }

        // Sets current player in PlayerContext
        game.getPlayerContext().setCurrPlayer(player);
        OfferCard currentOffer = null;

        // If not in extra pick, validate the pick against the current offer
        if (!extraPickActive) {
             currentOffer = game.getBoard().findTrackPosition(player);
            validatePick(game.getBoard(), cardId, currentOffer);
        }

        Card selectedCard = null;

        if(!skipAvailable){
            boolean isTop = !extraPickActive && game.getBoard().isCardTop(cardId);
            selectedCard = game.getBoard().takeCard(game.getPlayerContext(), cardId);

            if (!extraPickActive) {
                if (isTop) {
                    picksFromUp++;
                } else {
                    picksFromDown++;
                }
            }

            // Strategy registration to notificator center
            if (selectedCard.getStrategy() != null) {
                game.getPlayerContext().setCurrPlayer(player);
                selectedCard.getStrategy().registerTo(
                        game.getNotificatorCenter(), game.getPlayerContext());
                // Immediately activates OnPick strategies
                game.getNotificatorCenter().getPickNotificator()
                        .notify(game.getPlayerContext());
            }

            if (extraPickActive) {
                extraPickActive = false;
                extraPickPlayer = null;
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "END_TURN"));
                deltas.addAll(endPhase.endTurn(game));
                return deltas;
            }
        }

        // Checks the number of cards taken so far
        if (picksFromUp + picksFromDown >= currentOffer.getTotalPicks() || skipAvailable) {
            handleTotemReturn(game, currentOffer.returnTotem().get());
            totemReturned = true;

            // Resets for next player
            picksFromUp = 0;
            picksFromDown = 0;
            currIdx++;
        }

        // Handles end round
        if (currIdx >= actionOrder.size()) {
            checkExtraPick(game);

            if (!extraPickActive) {
                EndTurnPhase endPhase = new EndTurnPhase();
                game.setPhase(endPhase);
                deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "END_TURN"));
                deltas.addAll(endPhase.endTurn(game));
            }
            else deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "PLAYER_OFFER"));
        }
        else deltas.add(buildCardDelta(player, selectedCard, game, totemReturned, "PLAYER_OFFER"));

        totemReturned = false;
        skipAvailable = false;

        return deltas;
    }


    /**
     * Validates a player's attempt to pick a card against the game rules.
     * <p>
     * Ensures the player respects the limits of their offer slot (number of picks from
     * upper/lower rows). If the player attempts to "skip", the method verifies that no
     * valid Character cards are left in the required row.
     *
     * @param board  the current state of the game board
     * @param cardId the ID of the requested card, or "skip"
     * @param offer  the specific offer slot holding the player's totem
     * @throws InvalidActionException if the requested action violates any constraints
     */
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
                    throw new InvalidActionException("You must take a card from the upper row.");
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
                    throw new InvalidActionException("You must take a card from the lower row.");
                }
            }
        }

        boolean isTop = board.isCardTop(cardId);
        boolean isDown = board.isCardDown(cardId);

        if (!isTop && !isDown) {
            throw new InvalidActionException("The selected card is not on the board.");
        }
        if (isTop && offer.getNumUp() == 0)
            throw new InvalidActionException("You can't draw a card from the upper row.");

        if (isDown && offer.getNumDown() == 0)
            throw new InvalidActionException("You can't draw a card from the lower row.");

        if (isTop && picksFromUp >= offer.getNumUp()) {
            throw new InvalidActionException("You've already taken from the upper row.");
        }
        if (isDown && picksFromDown >= offer.getNumDown()) {
            throw new InvalidActionException("You've already taken from the lower row.");
        }
    }

    /**
     * Executes the mechanical return of a player's totem to their pool.
     * <p>
     * Automatically triggers the "OnTotemReturned" notificator, which may activate
     * specific strategic effects (e.g., gaining extra food upon retrieval).
     *
     * @param game   the main game instance
     * @param player the player receiving their totem back
     */
    private void handleTotemReturn(Game game, Player player) {
        game.getBoard().returnTotem(player);
        game.getPlayerContext().setCurrPlayer(player);
        game.getNotificatorCenter().getTotemReturnedNotificator()
                .notify(game.getPlayerContext());
    }

    /**
     * Checks all players at the end of the round to see if the "Extra Pick" effect is triggered.
     * <p>
     * Searches the player contexts for the {@code deservesExtraPick} flag (usually activated
     * by a specific building). If found, it activates the extra pick sub-phase.
     *
     * @param game the main game instance
     */
    private void checkExtraPick(Game game) {
        game.getPlayerContext().getPlayers().stream()
                .filter(Player::deservesExtraPick)
                .findFirst()
                .ifPresent(p -> {
                    extraPickActive = true;
                    extraPickPlayer = p;
                });
    }

    /**
     * Constructs the appropriate network delta to broadcast the result of a pick action.
     * <p>
     * Differentiates between skipped turns, building purchases, and character acquisitions
     * to package the correct updated lists and player stats for the clients.
     *
     * @param player        the player who performed the action
     * @param card          the specific card acquired, or null if the turn was skipped
     * @param game          the main game instance providing the updated board state
     * @param totemReturned true if the player's totem was returned during this action
     * @param phaseName     the identifier of the next game phase
     * @return a concrete implementation of {@link GameDelta} ready for broadcast
     */
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

    /**
     * Serializes this complex phase into a lightweight data transfer object.
     *
     * @return a {@link PlayerOfferPhaseSnapshot} capturing all internal counters and turn orders
     */
    @Override
    public PhaseSnapshot toSnapshot(){
        List<String> orderNicknames = actionOrder.stream()
                .map(Player::getNickname)
                .toList();

        return new PlayerOfferPhaseSnapshot(orderNicknames, currIdx, picksFromUp, picksFromDown, extraPickActive, extraPickPlayer != null ? extraPickPlayer.getNickname() : null, totemReturned);
    }
}
