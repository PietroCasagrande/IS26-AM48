// PhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;

public abstract class PhaseSnapshot implements Serializable {
    private final String phaseName;

    protected PhaseSnapshot(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() { return phaseName; }
}