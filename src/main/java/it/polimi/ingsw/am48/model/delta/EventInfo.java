package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class EventInfo implements Serializable {
    private final String eventType;

    @JsonCreator
    public EventInfo(@JsonProperty("eventType") String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() { return eventType; }
}
