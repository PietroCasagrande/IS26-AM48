// PhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;


/**
 * Base type for the serializable snapshot of a game phase. Each concrete
 * subclass corresponds to a specific phase of the game and carries the
 * phase-specific state that must survive persistence.
 *
 * <p>Snapshots are (de)serialized using Jackson polymorphic typing: a
 * {@code "phaseType"} property in the JSON selects the concrete subclass, as
 * declared by the {@link JsonSubTypes} mapping above. The {@link #phaseName}
 * field carries the same logical phase identifier as a plain string.
 * </p>
 *
 * @see GameSnapshot
 */
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

    /**
     * Creates a new phase snapshot with the given logical phase name.
     *
     * @param phaseName the identifier of the phase this snapshot represents
     */
    @JsonCreator
    protected PhaseSnapshot(
            @JsonProperty("phaseName") String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() { return phaseName; }
}