package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} sent when a player picks a building card from the shared
 * "showed" rows. It conveys which card was taken, the refreshed building rows,
 * the player's updated food and prestige points, whether the player returned
 * their totem (i.e. finished picking) and the resulting game phase.
 *
 * @see GameDelta
 * @see ClientModel
 */
public class BuildingCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperBuildingIds;
    private final List<String> updatedLowerBuildingIds;
    private final int updatedFood;        // cibo dopo pagamento edificio e ritorno totem
    private final int updatedPoints;      // pp dopo eventuale penalità cibo
    private final boolean totemReturned;  // true se il player ha finito i suoi pick
    private final String currentPhase;

    /**
     * Creates a new building-card-picked delta.
     *
     * @param playerNickname the nickname of the player who picked the card
     * @param cardId the identifier of the picked building card
     * @param updatedUpperBuildingIds the refreshed upper "showed" building row
     * @param updatedLowerBuildingIds the refreshed lower "showed" building row
     * @param updatedFood the player's food after paying for the building and
     *                    returning the totem
     * @param updatedPoints the player's prestige points after any food penalty
     * @param totemReturned {@code true} if the player has finished their picks
     *                      and the totem has been returned
     * @param currentPhase the game phase after applying this delta
     */
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

    /**
     * Applies the pick to the client model: adds the building to the player,
     * refreshes the showed building rows, updates the player's food and
     * prestige points, returns the totem if needed and sets the current phase.
     *
     * @param model the client model whose state must be updated
     */
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
