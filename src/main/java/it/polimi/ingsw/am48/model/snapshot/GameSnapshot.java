package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class GameSnapshot implements Serializable {
    private final String gameId;
    private final int numPlayers;
    private final PlayerContextSnapshot playerContext;
    private final int currentTurn;
    private final BoardSnapshot board;
    private final PhaseSnapshot phase;

    @JsonCreator
    public GameSnapshot(
            @JsonProperty("gameID") String gameId,
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("playerContext")  PlayerContextSnapshot playerContext,
            @JsonProperty("currentTurn") int currentTurn,
            @JsonProperty("board") BoardSnapshot board,
            @JsonProperty("phase") PhaseSnapshot phase) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.playerContext = playerContext;
        this.currentTurn = currentTurn;
        this.board = board;
        this.phase = phase;
    }

    public String getGameId() { return gameId; }
    public int getNumPlayers() { return numPlayers; }
    public PlayerContextSnapshot getPlayerContext() { return playerContext; }
    public int getCurrentTurn() { return currentTurn; }
    public BoardSnapshot getBoard() { return board; }
    public PhaseSnapshot getPhase() { return phase; }
}
