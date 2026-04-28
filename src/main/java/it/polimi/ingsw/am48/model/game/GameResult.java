package it.polimi.ingsw.am48.model.game;

import java.time.LocalDateTime;

public class GameResult {
    private final String gameId;
    private final String playerNickname;
    private final int finalScore;
    private final int numPlayers;
    private final LocalDateTime gameDate;

    public GameResult(String gameId, String playerNickname, int finalScore, int numPlayers, LocalDateTime gameDate){
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
