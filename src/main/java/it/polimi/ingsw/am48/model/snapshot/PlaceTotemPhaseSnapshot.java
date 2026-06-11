// PlaceTotemPhaseSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

/**
 * {@link PhaseSnapshot} for the totem-placement phase. It records the nicknames
 * of the players who have already placed their totem, so that the phase can
 * resume from the correct point after a restore.
 *
 * @see PhaseSnapshot
 */
public class PlaceTotemPhaseSnapshot extends PhaseSnapshot implements Serializable {
    private final Set<String> playersPlacedNicknames;

    /**
     * Creates a new totem-placement phase snapshot.
     *
     * @param playersPlacedNicknames the nicknames of the players who have
     *                               already placed their totem
     */
    @JsonCreator
    public PlaceTotemPhaseSnapshot(
            @JsonProperty("playersPlacedNicknames") Set<String> playersPlacedNicknames) {
        super("PLACE_TOTEM");
        this.playersPlacedNicknames = playersPlacedNicknames;
    }

    public Set<String> getPlayersPlacedNicknames() { return playersPlacedNicknames; }
}