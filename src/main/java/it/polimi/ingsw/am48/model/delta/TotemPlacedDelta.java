package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.util.List;

public class TotemPlacedDelta extends GameDelta{
    private final String playerNickname;
    private final char tileId;
    private final List<String> updatedOfferTurnCardOrder; // ordine senza questo player
    private final String currentPhase;

    @JsonCreator
    public TotemPlacedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("tileId") char tileId,
            @JsonProperty("updatedOfferTurnCardOrder") List<String> updatedOfferTurnCardOrder,
            @JsonProperty("currentPhase") String currentPhase) {
        this.playerNickname = playerNickname;
        this.tileId = tileId;
        this.updatedOfferTurnCardOrder = updatedOfferTurnCardOrder;
        this.currentPhase = currentPhase;
    }

    @Override
    public void applyTo(ClientModel model) {
        model.updateOfferTurnCardOrder(updatedOfferTurnCardOrder);
        model.placeTotemOnTrack(tileId, playerNickname);
        model.setPhase(currentPhase);
    }

    public String getPlayerNickname(){ return playerNickname; }
    public char getTileId(){ return tileId; }
    public List<String> getUpdatedOfferTurnCardOrder() {
        return updatedOfferTurnCardOrder;
    }
    public String getCurrentPhase() { return currentPhase; }
}
