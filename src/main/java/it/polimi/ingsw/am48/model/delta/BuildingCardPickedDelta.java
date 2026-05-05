package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class BuildingCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperBuildingIds;
    private final List<String> updatedLowerBuildingIds;
    private final int updatedFood;        // cibo dopo pagamento edificio e ritorno totem
    private final int updatedPoints;      // pp dopo eventuale penalità cibo
    private final boolean totemReturned;  // true se il player ha finito i suoi pick
    private final String currentPhase;

    @JsonCreator
    public BuildingCardPickedDelta(
            @JsonProperty("playerNickname")String playerNickname,
            @JsonProperty("cardId")String cardId,
            @JsonProperty("updatedUpperBuildingIds")List<String> updatedUpperBuildingIds,
            @JsonProperty("updatedLowerBuildingIds")List<String> updatedLowerBuildingIds,
            @JsonProperty("updatedFood") int updatedFood,
            @JsonProperty("updatedPoints") int updatedPoints,
            @JsonProperty("totemReturned") boolean totemReturned,
            @JsonProperty("currentPhase") String currentPhase) {
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.updatedUpperBuildingIds = updatedUpperBuildingIds;
        this.updatedLowerBuildingIds = updatedLowerBuildingIds;
        this.updatedFood = updatedFood;
        this.updatedPoints = updatedPoints;
        this.totemReturned = totemReturned;
        this.currentPhase = currentPhase;
    }

    @Override
    public void applyTo(ClientModel model) {
        model.addBuildingToPlayer(playerNickname, cardId);
        model.updateBuildingShowed(updatedUpperBuildingIds, updatedLowerBuildingIds);
        model.updatePlayerFood(playerNickname, updatedFood);
        model.updatePlayerPoints(playerNickname, updatedPoints);
        if (totemReturned) {
            model.returnTotemToTurnCard(playerNickname);
        }

        model.setPhase(currentPhase);
    }

    public String getPlayerNickname() {return playerNickname;}
    public String getCardId() {return cardId;}
    public List<String> getUpdatedUpperBuildingIds() {return updatedUpperBuildingIds;}
    public List<String> getUpdatedLowerBuildingIds() {return updatedLowerBuildingIds;}
    public int getUpdatedFood() {return updatedFood;}
    public int getUpdatedPoints() {return updatedPoints;}
    public boolean isTotemReturned() {return totemReturned;}
    public String getCurrentPhase() { return currentPhase; }
}
