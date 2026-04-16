package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CharacterCardPickedDelta extends GameDelta{
    private final String playerNickname;
    private final String cardId;
    private final List<String> updatedUpperTribeIds;  // fila superiore di showed aggiornata
    private final List<String> updatedLowerTribeIds;  // fila inferiore di showed aggiornata

    @JsonCreator
    public CharacterCardPickedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("cardId") String cardId,
            @JsonProperty("updatedUpperTribeIds") List<String> updatedUpperTribeIds,
            @JsonProperty("updatedLowerTribeIds") List<String> updatedLowerTribeIds) {
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.updatedUpperTribeIds = updatedUpperTribeIds;
        this.updatedLowerTribeIds = updatedLowerTribeIds;
    }

    public String getPlayerNickname() { return playerNickname; }
    public String getCardId() { return cardId; }
    public List<String> getUpdatedUpperRowIds() { return updatedUpperTribeIds; }
    public List<String> getUpdatedLowerRowIds() { return updatedLowerTribeIds; }
}
