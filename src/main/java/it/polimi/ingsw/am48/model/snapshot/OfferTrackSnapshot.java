
package it.polimi.ingsw.am48.model.snapshot;

import java.util.Map;

public class OfferTrackSnapshot {
    // key = lettera casella ("A", "B", ...), value = colore totem, assente = libera
    private final Map<Character, String> totemPositions;

    public OfferTrackSnapshot(Map<Character, String> totemPositions) {
        this.totemPositions = totemPositions;
    }

    public Map<Character, String> getTotemPositions() { return totemPositions; }
}