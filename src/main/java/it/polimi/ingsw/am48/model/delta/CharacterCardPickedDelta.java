package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} sent when a player picks a character card from the shared
 * "showed" tribe rows. It conveys which card was taken, the refreshed tribe
 * rows, the player's updated food and prestige points, whether the player
 * returned their totem (i.e. finished picking) and the resulting game phase.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class CharacterCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperTribeIds;  // fila superiore di showed aggiornata
    private final List<String> updatedLowerTribeIds;  // fila inferiore di showed aggiornata
    private final int updatedFood;
    private final int updatedPoints;
    private final boolean totemReturned;
    private final String currentPhase;

    /**
     * Creates a new character-card-picked delta.
     *
     * @param playerNickname the nickname of the player who picked the card
     * @param cardId the identifier of the picked character card
     * @param updatedUpperTribeIds the refreshed upper "showed" tribe row
     * @param updatedLowerTribeIds the refreshed lower "showed" tribe row
     * @param updatedFood the player's food after the pick
     * @param updatedPoints the player's prestige points after the pick
     * @param totemReturned {@code true} if the player has finished their picks
     *                      and the totem has been returned
     * @param currentPhase the game phase after applying this delta
     */
    @JsonCreator
    public CharacterCardPickedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("updatedUpperTribeIds") List<String> updatedUpperTribeIds,
            @JsonProperty("updatedLowerTribeIds") List<String> updatedLowerTribeIds,
            @JsonProperty("updatedFood") int updatedFood,
            @JsonProperty("updatedPoints") int updatedPoints,
            @JsonProperty("totemReturned") boolean totemReturned,
            @JsonProperty("currentPhase") String currentPhase){
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.updatedUpperTribeIds = updatedUpperTribeIds;
        this.updatedLowerTribeIds = updatedLowerTribeIds;
        this.updatedFood = updatedFood;
        this.updatedPoints = updatedPoints;
        this.totemReturned = totemReturned;
        this.currentPhase = currentPhase;
    }

    /**
     * Applies the pick to the client model: adds the card to the player's
     * tribe, refreshes the showed tribe rows, updates the player's food and
     * prestige points, returns the totem if needed and sets the current phase.
     *
     * @param model the client model whose state must be updated
     */
    @Override
    public void applyTo(ClientModel model) {
        model.addCardToPlayerTribe(playerNickname, cardId);
        model.updateTribeShowed(updatedUpperTribeIds, updatedLowerTribeIds);
        model.updatePlayerFood(playerNickname, updatedFood);
        model.updatePlayerPoints(playerNickname, updatedPoints);
        if (totemReturned) {
            model.returnTotemToTurnCard(playerNickname);
        }
        model.setPhase(currentPhase);
    }

    public String getPlayerNickname() { return playerNickname; }
    public String getCardId() { return cardId; }
    public List<String> getUpdatedUpperTribeIds() { return updatedUpperTribeIds; }
    public List<String> getUpdatedLowerTribeIds() { return updatedLowerTribeIds; }
    public int getUpdatedFood() { return updatedFood; }
    public int getUpdatedPoints() { return updatedPoints; }
    public boolean isTotemReturned() { return totemReturned; }
    public String getCurrentPhase() { return currentPhase; }
}
