package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;

public class EndGamePhaseSnapshot extends PhaseSnapshot implements Serializable {
    public EndGamePhaseSnapshot() {
        super("END_GAME");
    }
}