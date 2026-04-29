// PlaceTotemPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

public class PlaceTotemPhaseSnapshot extends PhaseSnapshot implements Serializable {
    private final Set<String> playersPlacedNicknames;

    @JsonCreator
    public PlaceTotemPhaseSnapshot(
            @JsonProperty("playersPlacedNicknames") Set<String> playersPlacedNicknames) {
        super("PLACE_TOTEM");
        this.playersPlacedNicknames = playersPlacedNicknames;
    }

    public Set<String> getPlayersPlacedNicknames() { return playersPlacedNicknames; }
}