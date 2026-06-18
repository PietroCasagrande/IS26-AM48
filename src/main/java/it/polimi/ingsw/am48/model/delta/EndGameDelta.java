package it.polimi.ingsw.am48.model.delta;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} broadcast when the game ends. It carries the final score
 * of every player and the nickname of the winner, so that each client can
 * align its local replica to the definitive result and switch into the
 * end-game state.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class EndGameDelta extends GameDelta{
    private final Map<String, Integer> finalScores;     // nickname -> final score
    private final String winnerNickname;

    /**
     * Creates a new end-game delta.
     *
     * @param finalScores map from each player's nickname to their final score
     * @param winnerNickname the nickname of the winning player
     */
    @JsonCreator
    public EndGameDelta(
            @JsonProperty("finalScores") Map<String, Integer> finalScores,
            @JsonProperty("winnerNickname") String winnerNickname) {
        this.finalScores = finalScores;
        this.winnerNickname = winnerNickname;
    }

    /**
     * Applies the end-game update to the client model: sets every player's
     * prestige points to their final score, records the winner and marks the
     * game as ended.
     *
     * @param model the client model whose state must be updated
     */
    // EndGame modifica pp di tutti i giocatori
    @Override
    public void applyTo(ClientModel model) {
        // System.out.println("DEBUG EndGameDelta: winner=" + winnerNickname + ", scores=" + finalScores);
        finalScores.keySet().forEach(nick -> {
            model.updatePlayerPoints(nick, finalScores.get(nick));
        });
        model.setWinnerNickname(winnerNickname);
        model.setGameEnded();
    };

    public Map<String, Integer> getFinalScores() {
        return finalScores;
    }
    public String getWinnerNickname() {
        return winnerNickname;
    }
}
