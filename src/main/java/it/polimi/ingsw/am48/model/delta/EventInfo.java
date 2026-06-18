package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Lightweight, serializable descriptor of a game event triggered during a
 * turn. It carries the event's type as a string so that it can be embedded in
 * an {@link EndTurnDelta} and transmitted to the clients, which use it to react
 * to (or display) the event.
 *
 * @see EndTurnDelta
 */
public class EventInfo implements Serializable {
    private final String eventType;

    /**
     * Creates a new event descriptor.
     *
     * @param eventType the identifier of the event type
     */
    @JsonCreator
    public EventInfo(@JsonProperty("eventType") String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() { return eventType; }
}
