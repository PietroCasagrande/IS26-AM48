package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;

public class EndTurnPhaseSnapshot extends PhaseSnapshot implements Serializable {
    public EndTurnPhaseSnapshot() {
        super("END_TURN");
    }
}
