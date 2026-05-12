package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class PlayerOfferPhaseSnapshot extends PhaseSnapshot implements Serializable {
    private final List<String> actionOrderNicknames;
    private final int currIdx;
    private final int picksFromUp;
    private final int picksFromDown;
    private final boolean extraPickActive;
    private final String extraPickPlayerNickname; // nickname, non Player
    private final boolean totemReturned;

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
