
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class OfferTrackSnapshot implements Serializable {
    // key = lettera casella ("A", "B", ...), value = colore totem, assente = libera
    private final Map<Character, String> totemPositions;

    @JsonCreator
    public OfferTrackSnapshot(
            @JsonProperty("totemPositions") Map<Character, String> totemPositions) {
        this.totemPositions = totemPositions;
    }

    public Map<Character, String> getTotemPositions() { return totemPositions; }
}