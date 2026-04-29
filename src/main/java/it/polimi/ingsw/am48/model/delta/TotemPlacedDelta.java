package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.util.List;

public class TotemPlacedDelta extends GameDelta{
    private final String playerNickname;
    private final char tileId;
    private final List<String> updatedOfferTurnCardOrder; // ordine senza questo player

    @JsonCreator
    public TotemPlacedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("tileId") char tileId,
            @JsonProperty("updatedOfferTurnCardOrder") List<String> updatedOfferTurnCardOrder) {
        this.playerNickname = playerNickname;
        this.tileId = tileId;
        this.updatedOfferTurnCardOrder = updatedOfferTurnCardOrder;
    }

    @Override
    public void applyTo(ClientModel model) {
        model.updateOfferTurnCardOrder(updatedOfferTurnCardOrder);
        model.placeTotemOnTrack(tileId, playerNickname);
    }

    public String getPlayerNickname(){ return playerNickname; }
    public char getTileId(){ return tileId; }
    public List<String> getUpdatedOfferTurnCardOrder() {
        return updatedOfferTurnCardOrder;
    }
}
