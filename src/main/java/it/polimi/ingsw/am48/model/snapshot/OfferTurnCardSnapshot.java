// OfferTurnCardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class OfferTurnCardSnapshot implements Serializable {
    private final List<String> totemOrder; // lista ordinata di colori totem

    @JsonCreator
    public OfferTurnCardSnapshot(
            @JsonProperty("totemOrder") List<String> totemOrder) {
        this.totemOrder = totemOrder;
    }

    public List<String> getTotemOrder() { return totemOrder; }
}
