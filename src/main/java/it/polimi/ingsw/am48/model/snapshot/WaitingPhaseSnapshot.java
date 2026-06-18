// WaitingPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * {@link PhaseSnapshot} for the phase in which the game is waiting for all
 * players to join. It carries no extra state beyond the phase identifier.
 *
 * @see PhaseSnapshot
 */
public class WaitingPhaseSnapshot extends PhaseSnapshot implements Serializable {
    /**
     * Creates a new waiting-phase snapshot.
     */
    @JsonCreator
    public WaitingPhaseSnapshot() {
        super("WAITING_FOR_PLAYERS");
    }
}
