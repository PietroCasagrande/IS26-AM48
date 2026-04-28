// OfferTurnCardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.util.List;

public class OfferTurnCardSnapshot {
    private final List<String> totemOrder; // lista ordinata di colori totem

    public OfferTurnCardSnapshot(List<String> totemOrder) {
        this.totemOrder = totemOrder;
    }

    public List<String> getTotemOrder() { return totemOrder; }
}
