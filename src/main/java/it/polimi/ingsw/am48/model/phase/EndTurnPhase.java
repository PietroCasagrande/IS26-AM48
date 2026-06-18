package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.EndTurnDelta;
import it.polimi.ingsw.am48.model.delta.EventInfo;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.snapshot.EndTurnPhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A transient resolution phase triggered at the end of every round.
 * <p>
 * This phase does not require direct player input. It automatically executes
 * the end-of-round bookkeeping mechanics, which include resolving active events,
 * shifting the board cards, incrementing the turn counter, and determining
 * whether the game continues to the next round or transitions to the final scoring phase.
 */
public class EndTurnPhase implements GamePhase {

    /**
     * Executes the comprehensive end-of-turn logic and resolves the game state.
     * <p>
     * The resolution order strictly follows the game rules:
     * <ol>
     * <li>Applies special end-game rules (e.g., registering upper row events on turn 10).</li>
     * <li>Triggers all active event effects via the Notificator Center.</li>
     * <li>Advances the board (shifting rows, refilling cards) unless it's the final turn.</li>
     * <li>Increments the turn counter and determines the next state (Place Totem vs. End Game).</li>
     * </ol>
     *
     * @param game the main game instance being updated
     * @return a list of network deltas containing the massive board and resource synchronization
     */
    public List<GameDelta> endTurn(Game game){
        List<GameDelta> deltas = new ArrayList<>();
        List<EventInfo> eventInfos = new ArrayList<>();

        // At the last turn, also register events from the upper row
        if(game.getCurrentTurn() == 10){
            game.getBoard().getTribeShowed().registerUpper(
                    game.getNotificatorCenter(), game.getPlayerContext());
        }

        // resolve events
        for(EventType e : EventType.values()){
            game.getNotificatorCenter().getEventNotificator().notify(e, game.getPlayerContext());
            eventInfos.add(new EventInfo(e.name()));
        }

        // board update after solving events
        if(game.getCurrentTurn() < 10) game.getBoard().endTurn(game.getNotificatorCenter(), game.getPlayerContext());

        // turn increment
        game.incrementTurn();

        // end game check
        if(game.getCurrentTurn() > 10){
            EndGamePhase endGamePhase = new EndGamePhase();
            game.setPhase(endGamePhase);
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "END_GAME", eventInfos, game.getBoard().getCurrEraIndex()));
            deltas.add(endGamePhase.resolveEndGame(game));
        } else {
            game.setPhase(new PlaceTotemPhase());
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "PLACE_TOTEM", eventInfos, game.getBoard().getCurrEraIndex()));
        }

        return deltas;
    }

    /**
     * Compiles a massive synchronization delta to update clients with the fresh round state.
     * <p>
     * This private helper gathers the updated resources for all players (after events
     * have modified them) and the new IDs of all cards present on the board after
     * the row shifting and refilling procedures.
     *
     * @param game       the current game instance
     * @param newTurn    the newly incremented turn number
     * @param newPhase   the name of the phase the game is transitioning into
     * @param eventInfos the list of events that were resolved during this turn transition
     * @param newEra     the current era index of the board
     * @return a fully populated {@link EndTurnDelta} ready for broadcast
     */
    private EndTurnDelta buildEndTurnDelta(Game game, int newTurn, String newPhase,List<EventInfo> eventInfos, int newEra) {
        // Updated resources for each player (after events)
        Map<String, Integer> updatedFood = new HashMap<>();
        Map<String, Integer> updatedPrestige = new HashMap<>();
        game.getPlayerContext().getPlayers().forEach(p -> {
            updatedFood.put(p.getNickname(), p.getFood());
            updatedPrestige.put(p.getNickname(), p.getPoints());
        });

        // New cards on board after shiftRow
        List<String> newUpperRowIds = game.getBoard().getTribeShowed()
                .getUpperList().stream().map(Card::getCardId).toList();
        List<String> newLowerRowIds = game.getBoard().getTribeShowed()
                .getLowerList().stream().map(Card::getCardId).toList();
        List<String> newBuildingUpperIds = game.getBoard().getBuildingShowed()
                .getUpperList().stream().map(Card::getCardId).toList();
        List<String> newBuildingLowerIds = game.getBoard().getBuildingShowed()
                .getLowerList().stream().map(Card::getCardId).toList();

        return new EndTurnDelta(
                updatedFood,
                updatedPrestige,
                newUpperRowIds,
                newLowerRowIds,
                newBuildingUpperIds,
                newBuildingLowerIds,
                newTurn,
                newPhase,
                eventInfos,
                newEra
        );
    }

    /**
     * Serializes this phase into a lightweight data transfer object.
     * <p>
     * Note: Because this is a transient phase that resolves immediately,
     * its snapshot is mostly symbolic and cannot be actively restored during a crash recovery.
     *
     * @return an {@link EndTurnPhaseSnapshot}
     */
    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndTurnPhaseSnapshot();
    }
}
