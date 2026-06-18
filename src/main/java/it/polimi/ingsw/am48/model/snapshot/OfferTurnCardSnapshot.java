// OfferTurnCardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable, immutable snapshot of the turn-order card. It holds the ordered
 * list of totems (by color) that defines the players' turn order.
 *
 * @see BoardSnapshot
 */
public class OfferTurnCardSnapshot implements Serializable {
    private final List<String> totemOrder; // lista ordinata di colori totem

    /**
     * Creates a new turn-order card snapshot.
     *
     * @param totemOrder the ordered list of totem colors defining turn order
     */
    @JsonCreator
    public OfferTurnCardSnapshot(
            @JsonProperty("totemOrder") List<String> totemOrder) {
        this.totemOrder = totemOrder;
    }

    public List<String> getTotemOrder() { return totemOrder; }
}
