package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} sent when a player skips their action. It carries the
 * player's nickname, whether their totem was returned as a result and the
 * resulting game phase.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class SkipDelta extends GameDelta {
    private final String playerNickname;
    private final boolean totemReturned;
    private final String currentPhase;

    /**
     * Creates a new skip delta.
     *
     * @param playerNickname the nickname of the player who skipped
     * @param totemReturned {@code true} if the player's totem has been returned
     * @param currentPhase the game phase after applying this delta
     */
    @JsonCreator
    public SkipDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("totemReturned") boolean totemReturned,
            @JsonProperty("currentPhase") String currentPhase) {
        this.playerNickname = playerNickname;
        this.totemReturned = totemReturned;
        this.currentPhase = currentPhase;
    }

    /**
     * Applies the skip to the client model: returns the player's totem if
     * needed and sets the current phase.
     *
     * @param model the client model whose state must be updated
     */
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