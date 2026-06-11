package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable, immutable snapshot of the player context. It holds the snapshot
 * of every {@link PlayerSnapshot player} and the nickname of the player whose
 * turn it currently is.
 *
 * @see GameSnapshot
 * @see PlayerSnapshot
 */
public class PlayerContextSnapshot implements Serializable {
    List<PlayerSnapshot> players;
    String currPlayerNickname;

    /**
     * Creates a new player-context snapshot.
     *
     * @param players the snapshots of all players in the game
     * @param currPlayerNickname the nickname of the current player
     */
    @JsonCreator
    public PlayerContextSnapshot(
            @JsonProperty("players") List<PlayerSnapshot> players,
            @JsonProperty("currPlayerNickname") String currPlayerNickname) {
        this.players = players;
        this.currPlayerNickname = currPlayerNickname;
    }

    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    public String getCurrPlayerNickname() {
        return currPlayerNickname;
    }
}
