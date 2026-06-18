package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.EndGameDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.EndGamePhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the terminal phase of the game.
 * <p>
 * This transient phase is triggered automatically after the final round concludes.
 * It is responsible for orchestrating the final scoring pipeline: activating
 * end-game specific card effects, computing the definitive tribe scores, and
 * declaring the absolute winner before broadcasting the results to all clients.
 */
public class EndGamePhase implements GamePhase {

    /**
     * Executes the final scoring procedures and determines the winner of the match.
     * <p>
     * The resolution strictly follows this pipeline:
     * <ol>
     * <li><b>Activation:</b> Triggers the EndGame notificator, allowing any specific
     *     card strategies (e.g., conditional victory points) to apply their effects.</li>
     * <li><b>Computation:</b> Commands each player's tribe to calculate its total end-game score.</li>
     * <li><b>Aggregation:</b> Collects the final point totals into a single mapping.</li>
     * <li><b>Declaration:</b> Determines the winning player based on the game's tie-breaker rules.</li>
     * </ol>
     *
     * @param game the main game instance ending its lifecycle
     * @return an {@link EndGameDelta} containing the comprehensive final scores and the winner's nickname
     */
    public GameDelta resolveEndGame(Game game){
        // activating end-game strategies
        game.getNotificatorCenter().getEndGameNotificator().notify(game.getPlayerContext());

        // computing end-game points for each player
        game.getPlayerContext().getPlayers()
                .forEach(p -> p.getTribe().computeTotalEndGameScore());

        // building the final scores map
        Map<String, Integer> finalScores = new HashMap<>();
        game.getPlayerContext().getPlayers()
                .forEach(p -> finalScores.put(p.getNickname(), p.getPoints()));

        // finding game's winner
        Player winner = game.findWinner();

        // returns the Delta containing the final score of each player, and winner's nickname
        return new EndGameDelta(finalScores, winner.getNickname());
    }

    /**
     * Serializes this terminal phase into a lightweight data transfer object.
     *
     * @return an {@link EndGamePhaseSnapshot}
     */
    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndGamePhaseSnapshot();
    }
}
