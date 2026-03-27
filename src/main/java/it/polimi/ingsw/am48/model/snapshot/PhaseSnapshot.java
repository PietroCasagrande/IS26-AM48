// PhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

public abstract class PhaseSnapshot {
    private final String phaseName;

    protected PhaseSnapshot(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseName() { return phaseName; }
}