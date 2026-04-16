package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildingCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperBuildingIds;
    private final List<String> updatedLowerBuildingIds;

    @JsonCreator
    public BuildingCardPickedDelta(
            @JsonProperty("playerNickname")String playerNickname,
            @JsonProperty("cardId")String cardId,
            @JsonProperty("updatedUpperBuildingIds")List<String> updatedUpperBuildingIds,
            @JsonProperty("updatedLowerBuildingIds")List<String> updatedLowerBuildingIds) {
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
