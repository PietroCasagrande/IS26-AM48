package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Serializable, immutable snapshot of the whole game state at a given instant.
 * It is the root of the snapshot tree and aggregates the {@link PlayerContextSnapshot
 * player context}, the {@link BoardSnapshot board} and the current
 * {@link PhaseSnapshot phase}, together with scalar metadata such as the game
 * id, the number of players and the current turn.
 *
 * <p>Snapshots are produced when persisting a game and consumed when restoring
 * it after a crash, so that the full game can be reconstructed from disk.
 * </p>
 *
 * @see BoardSnapshot
 * @see PlayerContextSnapshot
 * @see PhaseSnapshot
 */
public class GameSnapshot implements Serializable {
    private final String gameId;
    private final int numPlayers;
    private final PlayerContextSnapshot playerContext;
    private final int currentTurn;
    private final BoardSnapshot board;
    private final PhaseSnapshot phase;

    /**
     * Creates a new game snapshot.
     *
     * @param gameId the unique identifier of the game
     * @param numPlayers the number of players in the game
     * @param playerContext the snapshot of the player context (players and
     *                      current player)
     * @param currentTurn the index of the current turn
     * @param board the snapshot of the board state
     * @param phase the snapshot of the current game phase
     */
    @JsonCreator
    public GameSnapshot(
            @JsonProperty("gameId") String gameId,
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
