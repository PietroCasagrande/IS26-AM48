package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Serializable, immutable snapshot of a single player. It holds the player's
 * nickname, totem color and the snapshot of their {@link TribeSnapshot tribe}.
 *
 * @see PlayerContextSnapshot
 * @see TribeSnapshot
 */
public class PlayerSnapshot implements Serializable {
    private final String nickname;
    private final String totemColor;
    private final TribeSnapshot tribe;

    /**
     * Creates a new player snapshot.
     *
     * @param nickname the player's nickname
     * @param totemColor the color of the player's totem
     * @param tribe the snapshot of the player's tribe
     */
    @JsonCreator
    public PlayerSnapshot(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("totemColor") String totemColor,
            @JsonProperty("tribe") TribeSnapshot tribe) {
        this.nickname = nickname;
        this.totemColor = totemColor;
        this.tribe = tribe;
    }

    public String getNickname() { return nickname; }
    public String getTotemColor() { return totemColor; }
    public TribeSnapshot getTribe() { return tribe; }
}