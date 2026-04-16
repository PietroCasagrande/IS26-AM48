package it.polimi.ingsw.am48.model.snapshot;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameSnapshot {
    private final String gameId;
    private final int numPlayers;
    private final int currentTurn;
    private final List<PlayerSnapshot> players;
    private final BoardSnapshot board;
    private final PhaseSnapshot phase;

    @JsonCreator
    public GameSnapshot(
            @JsonProperty("gameID") String gameId,
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("currentTurn") int currentTurn,
            @JsonProperty("players") List<PlayerSnapshot> players,
            @JsonProperty("board") BoardSnapshot board,
            @JsonProperty("phase") PhaseSnapshot phase) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.currentTurn = currentTurn;
        this.players = players;
        this.board = board;
        this.phase = phase;
    }

    public String getGameId() { return gameId; }
    public int getNumPlayers() { return numPlayers; }
    public int getCurrentTurn() { return currentTurn; }
    public List<PlayerSnapshot> getPlayers() { return players; }
    public BoardSnapshot getBoard() { return board; }
    public PhaseSnapshot getPhase() { return phase; }
}
