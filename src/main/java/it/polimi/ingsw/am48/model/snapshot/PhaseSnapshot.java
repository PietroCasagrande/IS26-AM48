// PhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "phaseType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = WaitingPhaseSnapshot.class, name = "WAITING_FOR_PLAYERS"),
        @JsonSubTypes.Type(value = PlaceTotemPhaseSnapshot.class, name = "PLACE_TOTEM"),
        @JsonSubTypes.Type(value = PlayerOfferPhaseSnapshot.class, name = "PLAYER_OFFER"),
        @JsonSubTypes.Type(value = EndTurnPhaseSnapshot.class, name = "END_TURN"),
        @JsonSubTypes.Type(value = EndGamePhaseSnapshot.class, name = "END_GAME")
})
public abstract class PhaseSnapshot implements Serializable {
    private final String phaseName;

    @JsonCreator
    protected PhaseSnapshot(
            @JsonProperty("phaseName") String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() { return phaseName; }
}