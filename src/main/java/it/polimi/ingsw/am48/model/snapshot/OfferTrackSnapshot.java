
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * Serializable, immutable snapshot of the offer track. It maps each track tile
 * (identified by its letter) to the nickname of the player whose totem is
 * placed there; a missing key means the tile is free.
 *
 * @see BoardSnapshot
 */
public class OfferTrackSnapshot implements Serializable {
    // key = lettera casella ("A", "B", ...), value = nickname player, assente = libera
    private final Map<Character, String> totemPositions;

    /**
     * Creates a new offer-track snapshot.
     *
     * @param totemPositions map from track tile letter to the nickname of the
     *                       player occupying it; tiles absent from the map are
     *                       free
     */
    @JsonCreator
    public OfferTrackSnapshot(
            @JsonProperty("totemPositions") Map<Character, String> totemPositions) {
        this.totemPositions = totemPositions;
    }

    public Map<Character, String> getTotemPositions() { return totemPositions; }
}