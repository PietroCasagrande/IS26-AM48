package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * {@link PhaseSnapshot} for the end-of-turn phase. It carries no extra state
 * beyond the phase identifier.
 *
 * @see PhaseSnapshot
 */
public class EndTurnPhaseSnapshot extends PhaseSnapshot implements Serializable {
    /**
     * Creates a new end-of-turn phase snapshot.
     */
    @JsonCreator
    public EndTurnPhaseSnapshot() {
        super("END_TURN");
    }
}
