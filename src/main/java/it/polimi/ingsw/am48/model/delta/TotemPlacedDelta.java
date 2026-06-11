package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.util.List;

/**
 * {@link GameDelta} sent when a player places a totem on a track tile. It
 * carries the player's nickname, the target tile, the updated turn-card offer
 * order (without this player) and the resulting game phase.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class TotemPlacedDelta extends GameDelta{
    private final String playerNickname;
    private final char tileId;
    private final List<String> updatedOfferTurnCardOrder; // ordine senza questo player
    private final String currentPhase;

    /**
     * Creates a new totem-placed delta.
     *
     * @param playerNickname the nickname of the player who placed the totem
     * @param tileId the identifier of the track tile where the totem is placed
     * @param updatedOfferTurnCardOrder the turn-card offer order updated to
     *                                  exclude this player
     * @param currentPhase the game phase after applying this delta
     */
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

    /**
     * Applies the placement to the client model: updates the turn-card offer
     * order, places the totem on the track tile and sets the current phase.
     *
     * @param model the client model whose state must be updated
     */
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
