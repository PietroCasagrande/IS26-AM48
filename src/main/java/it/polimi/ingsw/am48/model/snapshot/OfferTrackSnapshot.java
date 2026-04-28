
package it.polimi.ingsw.am48.model.snapshot;

import java.util.Map;

public class OfferTrackSnapshot {
    // key = lettera casella ("A", "B", ...), value = colore totem, assente = libera
    private final Map<String, String> totemPositions;

    public OfferTrackSnapshot(Map<String, String> totemPositions) {
        this.totemPositions = totemPositions;
    }

    public Map<String, String> getTotemPositions() { return totemPositions; }
}