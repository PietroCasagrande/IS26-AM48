package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class OfferCardADelta extends GameDelta {
    private final String playerNickname;
    private final int updatedFood;

    @JsonCreator
    public OfferCardADelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("updatedFood") int updatedFood) {
                this.playerNickname = playerNickname;
                this.updatedFood = updatedFood;
    }

    @Override
    public void applyTo(ClientModel model) {
        model.returnTotemToTurnCard(playerNickname);
        model.updatePlayerFood(playerNickname, updatedFood);
    }
}
