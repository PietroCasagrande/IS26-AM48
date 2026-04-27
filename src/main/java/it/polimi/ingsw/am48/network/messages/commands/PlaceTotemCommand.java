package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

public class PlaceTotemCommand extends ClientCommand {
    private char position;

    @JsonCreator
    public PlaceTotemCommand(@JsonProperty("position") char position) {
        this.position = position;
    }
    @Override
    public void execute(SocketClientHandler handler) {
        try{
            List<GameDelta> deltas = handler.getController().handlePlaceTotem(handler.getNickname(), position);
            List<String> recipients = handler.getController().getPlayersInGame(handler.getNickname());
            for (GameDelta d : deltas) {
                handler.getServer().broadcastToGame(recipients, d);
            }
        } catch(Exception e) {
            try{ handler.getSelfView().reportError(e.getMessage()); }
            catch(Exception ex){ ex.printStackTrace(); } }
    }
}
