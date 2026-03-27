package it.polimi.ingsw.am48.model.snapshot;

import java.util.List;

public class PlayerOfferPhaseSnapshot extends PhaseSnapshot {
    private final List<String> actionOrderNicknames;
    private final int currIdx;

    public PlayerOfferPhaseSnapshot(List<String> actionOrderNicknames, int currIdx) {
        super("PLAYER_OFFER");
        this.actionOrderNicknames = actionOrderNicknames;
        this.currIdx = currIdx;
    }

    public List<String> getActionOrderNicknames() { return actionOrderNicknames; }
    public int getCurrIdx() { return currIdx; }
}
