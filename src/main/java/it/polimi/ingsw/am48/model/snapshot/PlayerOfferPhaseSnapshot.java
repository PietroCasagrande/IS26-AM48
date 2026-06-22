package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * {@link PhaseSnapshot} for the player-offer phase, in which players pick cards
 * in turn. It records the action order, the index of the current player, how
 * many picks are still allowed from the upper and lower rows, and the state of
 * any active extra-pick right, so that the phase can resume exactly after a
 * restore.
 *
 * @see PhaseSnapshot
 */
public class PlayerOfferPhaseSnapshot extends PhaseSnapshot implements Serializable {
    private final List<String> actionOrderNicknames;
    private final int currIdx;
    private final int picksFromUp;
    private final int picksFromDown;
    private final boolean extraPickActive;
    private final String extraPickPlayerNickname; // nickname, not Player
    private final boolean totemReturned;

    /**
     * Creates a new player-offer phase snapshot.
     *
     * @param actionOrderNicknames the nicknames of the players in action order
     * @param currIdx the index of the current player within the action order
     * @param picksFromUp the number of picks still allowed from the upper row
     * @param picksFromDown the number of picks still allowed from the lower row
     * @param extraPickActive {@code true} if an extra-pick right is currently
     *                        active
     * @param extraPickPlayerNickname the nickname of the player holding the
     *                                extra pick, or {@code null} if none
     * @param totemReturned {@code true} if the current player's totem has been
     *                      returned
     */
    @JsonCreator
    public PlayerOfferPhaseSnapshot(
            @JsonProperty("actionOrderNicknames") List<String> actionOrderNicknames,
            @JsonProperty("currIdx") int currIdx,
            @JsonProperty("picksFromUp") int picksFromUp,
            @JsonProperty("picksFromDown") int picksFromDown,
            @JsonProperty("extraPickActive") boolean extraPickActive,
            @JsonProperty("extraPickPlayerNickname") String extraPickPlayerNickname,
            @JsonProperty("totemReturned") boolean totemReturned) {

        super("PLAYER_OFFER");
        this.actionOrderNicknames = actionOrderNicknames;
        this.currIdx = currIdx;
        this.picksFromUp = picksFromUp;
        this.picksFromDown = picksFromDown;
        this.extraPickActive = extraPickActive;
        this.extraPickPlayerNickname = extraPickPlayerNickname;
        this.totemReturned = totemReturned;
    }

    public List<String> getActionOrderNicknames() { return actionOrderNicknames; }
    public int getCurrIdx() { return currIdx; }
    public int getPicksFromUp() { return picksFromUp; }
    public int getPicksFromDown() { return picksFromDown; }
    public boolean getExtraPickActive() { return extraPickActive; }
    public String getExtraPickPlayerNickname() { return extraPickPlayerNickname; }
    public boolean getTotemReturned() { return totemReturned; }
}
