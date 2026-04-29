package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

public class EndTurnPhaseSnapshot extends PhaseSnapshot implements Serializable {
    @JsonCreator
    public EndTurnPhaseSnapshot() {
        super("END_TURN");
    }
}
