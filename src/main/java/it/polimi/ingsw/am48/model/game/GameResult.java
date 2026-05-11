package it.polimi.ingsw.am48.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GameResult implements Serializable {
    private final String gameId;
    private final String playerNickname;
    private final int finalScore;
    private final int numPlayers;
    private final LocalDateTime gameDate;

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
