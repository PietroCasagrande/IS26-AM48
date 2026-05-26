package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class SkipDelta extends GameDelta {
    private final String playerNickname;
    private final boolean totemReturned;
    private final String currentPhase;

    @JsonCreator
    public SkipDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("totemReturned") boolean totemReturned,
            @JsonProperty("currentPhase") String currentPhase) {
        this.playerNickname = playerNickname;
        this.totemReturned = totemReturned;
        this.currentPhase = currentPhase;
    }

    @Override
    public void applyTo(ClientModel model) {
        if (totemReturned) {
            model.returnTotemToTurnCard(playerNickname);
        }
        model.setPhase(currentPhase);
    }

    public String getPlayerNickname() { return playerNickname; }
    public boolean isTotemReturned() { return totemReturned; }
    public String getCurrentPhase() { return currentPhase; }
}