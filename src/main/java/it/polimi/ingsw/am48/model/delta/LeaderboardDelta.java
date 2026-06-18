package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.io.Serializable;
import java.util.List;

/**
 * {@link GameDelta} that delivers the final leaderboard to the clients. It
 * wraps the ordered list of {@link GameResult} entries computed by the server
 * once the game is over.
 *
 * @see GameDelta
 * @see GameResult
 * @see ClientModel
 */
public class LeaderboardDelta extends GameDelta implements Serializable {
    private final List<GameResult> leaderboard;

    /**
     * Creates a new leaderboard delta.
     *
     * @param leaderboard the ordered list of per-player game results
     */
    @JsonCreator
    public LeaderboardDelta(@JsonProperty("leaderboard") List<GameResult> leaderboard) {
        this.leaderboard = leaderboard;
    }

    /**
     * Applies the update to the client model by storing the final leaderboard.
     *
     * @param model the client model whose state must be updated
     */
    @Override
    public void applyTo(ClientModel model){
        model.setLeaderboard(leaderboard);
    }

    public List<GameResult> getLeaderboard() { return leaderboard; }
}
