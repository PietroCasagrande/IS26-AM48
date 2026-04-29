package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class CharacterCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperTribeIds;  // fila superiore di showed aggiornata
    private final List<String> updatedLowerTribeIds;  // fila inferiore di showed aggiornata
    private final int updatedFood;
    private final int updatedPoints;
    private final boolean totemReturned;

    @JsonCreator
    public CharacterCardPickedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("updatedUpperTribeIds") List<String> updatedUpperTribeIds,
            @JsonProperty("updatedLowerTribeIds") List<String> updatedLowerTribeIds,
            @JsonProperty("updatedFood") int updatedFood,
            @JsonProperty("updatedPoints") int updatedPoints,
            @JsonProperty("totemReturned") boolean totemReturned ){
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.updatedUpperTribeIds = updatedUpperTribeIds;
        this.updatedLowerTribeIds = updatedLowerTribeIds;
        this.updatedFood = updatedFood;
        this.updatedPoints = updatedPoints;
        this.totemReturned = totemReturned;
    }

    @Override
    public void applyTo(ClientModel model) {
        model.addCardToPlayerTribe(playerNickname, cardId);
        model.updateTribeShowed(updatedUpperTribeIds, updatedLowerTribeIds);
        model.updatePlayerFood(playerNickname, updatedFood);
        model.updatePlayerPoints(playerNickname, updatedPoints);
        if (totemReturned) {
            model.returnTotemToTurnCard(playerNickname);
        }
    }

    public String getPlayerNickname() { return playerNickname; }
    public String getCardId() { return cardId; }
    public List<String> getUpdatedUpperTribeIds() { return updatedUpperTribeIds; }
    public List<String> getUpdatedLowerTribeIds() { return updatedLowerTribeIds; }
    public int getUpdatedFood() { return updatedFood; }
    public int getUpdatedPoints() { return updatedPoints; }
    public boolean isTotemReturned() { return totemReturned; }
}
