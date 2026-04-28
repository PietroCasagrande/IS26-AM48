package it.polimi.ingsw.am48.model.delta;

import java.util.List;

public class BuildingCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperBuildingIds;
    private final List<String> updatedLowerBuildingIds;

    public BuildingCardPickedDelta(String playerNickname, String cardId, List<String> updatedUpperBuildingIds, List<String> updatedLowerBuildingIds) {
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.updatedUpperBuildingIds = updatedUpperBuildingIds;
        this.updatedLowerBuildingIds = updatedLowerBuildingIds;
    }

    // getter anche qui per tutti i campi, teoricamente vengono usati dal controller
    public String getPlayerNickname() { return playerNickname; }
    public String getCardId() { return cardId; }
    public List<String> getUpdatedUpperRowIds() { return updatedUpperBuildingIds; }
    public List<String> getUpdatedLowerRowIds() { return updatedLowerBuildingIds; }
}
