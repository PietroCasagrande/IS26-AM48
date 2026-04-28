// PlaceTotemPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;
import java.util.Set;

public class PlaceTotemPhaseSnapshot extends PhaseSnapshot implements Serializable {
    private final Set<String> playersPlacedNicknames;

    public PlaceTotemPhaseSnapshot(Set<String> playersPlacedNicknames) {
        super("PLACE_TOTEM");
        this.playersPlacedNicknames = playersPlacedNicknames;
    }

    public Set<String> getPlayersPlacedNicknames() { return playersPlacedNicknames; }
}