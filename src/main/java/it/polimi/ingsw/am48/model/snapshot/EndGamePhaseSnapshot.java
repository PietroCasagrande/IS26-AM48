package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * {@link PhaseSnapshot} for the end-of-game phase. It carries no extra state
 * beyond the phase identifier.
 *
 * @see PhaseSnapshot
 */
public class EndGamePhaseSnapshot extends PhaseSnapshot implements Serializable {
    /**
     * Creates a new end-of-game phase snapshot.
     */
    @JsonCreator
    public EndGamePhaseSnapshot() {
        super("END_GAME");
    }
}