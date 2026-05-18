package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class EventInfo implements Serializable {
    private final String eventType;
    private final Map<String, Integer> foodDeltas;
    private final Map<String, Integer> pointsDeltas;

    @JsonCreator
    public EventInfo(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("foodDeltas") Map<String, Integer> foodDeltas,
            @JsonProperty("pointsDeltas") Map<String, Integer> pointsDeltas) {
        this.eventType = eventType;
        this.foodDeltas = foodDeltas;
        this.pointsDeltas = pointsDeltas;
    }

    public String getEventType() { return eventType; }
    public Map<String, Integer> getFoodDeltas() { return foodDeltas; }
    public Map<String, Integer> getPointsDeltas() { return pointsDeltas; }
}
