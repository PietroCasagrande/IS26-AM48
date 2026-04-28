package it.polimi.ingsw.am48.model.delta;

public class TotemPlacedDelta extends GameDelta{
    private final String playerNickname;
    private final char tileId;

    public TotemPlacedDelta(String playerNickname, char tileId){
        this.playerNickname = playerNickname;
        this.tileId = tileId;
    }

    public String getPlayerNickname(){ return playerNickname; }
    public char getTileId(){ return tileId; }
}
