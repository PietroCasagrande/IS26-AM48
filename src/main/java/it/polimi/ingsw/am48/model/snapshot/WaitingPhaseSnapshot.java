// WaitingPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;

public class WaitingPhaseSnapshot extends PhaseSnapshot implements Serializable {
    public WaitingPhaseSnapshot() {
        super("WAITING_FOR_PLAYERS");
    }
}
