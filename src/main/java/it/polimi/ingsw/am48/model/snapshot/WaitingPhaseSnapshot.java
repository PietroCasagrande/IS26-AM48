// WaitingPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

public class WaitingPhaseSnapshot extends PhaseSnapshot implements Serializable {
    @JsonCreator
    public WaitingPhaseSnapshot() {
        super("WAITING_FOR_PLAYERS");
    }
}
