package it.polimi.ingsw.am48.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable value object representing a single player's outcome in a completed game.
 *
 * <p>Instances of this class serve two purposes:
 * <ul>
 *   <li><b>Persistence:</b> written to and read from the MySQL {@code game_results} table
 *       via {@link it.polimi.ingsw.am48.repository.LeaderboardRepository}.</li>
 *   <li><b>Network transport:</b> included in
 *       {@link it.polimi.ingsw.am48.model.delta.LeaderboardDelta} and forwarded to clients
 *       at game end. The {@link JsonCreator} annotation supports Jackson deserialisation over
 *       Socket connections; {@link Serializable} supports native Java serialisation over RMI.</li>
 * </ul>
 */
public class GameResult implements Serializable {
    private final String gameId;
    private final String playerNickname;
    private final int finalScore;
    private final int numPlayers;
    private final LocalDateTime gameDate;

    /**
     * Constructs a {@code GameResult}. The {@link JsonCreator} annotation enables Jackson
     * to deserialise instances when they are received over a Socket connection.
     *
     * @param gameId         the unique identifier of the game (e.g. {@code "GAME-1"})
     * @param playerNickname the nickname of the player this result refers to
     * @param finalScore     the player's prestige-point total at game end
     * @param numPlayers     the number of players who participated in the game
     * @param gameDate       the timestamp at which the game ended
     */
    @JsonCreator
    public GameResult(
            @JsonProperty("gameId") String gameId,
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("finalScore") int finalScore,
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("gameDate") LocalDateTime gameDate){
        this.gameId = gameId;
        this.playerNickname = playerNickname;
        this.finalScore = finalScore;
        this.numPlayers = numPlayers;
        this.gameDate = gameDate;
    }

    public String getGameId(){ return gameId; }
    public String getPlayerNickname(){ return playerNickname; }
    public int getFinalScore(){ return finalScore; }
    public int getNumPlayers(){ return numPlayers; }
    public LocalDateTime getGameDate() { return gameDate; }
}
