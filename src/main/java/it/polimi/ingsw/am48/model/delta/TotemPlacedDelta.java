package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TotemPlacedDelta extends GameDelta{
    private final String playerNickname;
    private final char tileId;

    @JsonCreator
    public TotemPlacedDelta(
            @JsonProperty("playerNickname") String playerNickname,
            @JsonProperty("tileId") char tileId){
        this.playerNickname = playerNickname;
        this.tileId = tileId;
    }

    public String getPlayerNickname(){ return playerNickname; }
    public char getTileId(){ return tileId; }
}
