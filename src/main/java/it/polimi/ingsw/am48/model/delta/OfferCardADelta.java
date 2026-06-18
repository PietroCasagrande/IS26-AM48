package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} sent when a player chooses the "offer" action (card A),
 * gaining food in exchange for returning their totem. It carries the player's
 * nickname and their updated food amount.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class OfferCardADelta extends GameDelta {
    private final String playerNickname;
    private final int updatedFood;

    /**
     * Creates a new offer (card A) delta.
     *
     * @param playerNickname the nickname of the player who performed the offer
     * @param updatedFood the player's food after the offer
     */
    @JsonCreator
    public OfferCardADelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("updatedFood") int updatedFood) {
                this.playerNickname = playerNickname;
                this.updatedFood = updatedFood;
    }

    /**
     * Applies the offer to the client model: returns the player's totem to the
     * turn card and updates their food amount.
     *
     * @param model the client model whose state must be updated
     */
    @Override
    public void applyTo(ClientModel model) {
        model.returnTotemToTurnCard(playerNickname);
        model.updatePlayerFood(playerNickname, updatedFood);
    }

    public String getPlayerNickname() {
        return playerNickname;
    }
    public int getUpdatedFood() {
        return updatedFood;
    }
}
