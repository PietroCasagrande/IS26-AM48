package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

public class TakeCardCommand extends ClientCommand {
    private String cardId;

    @JsonCreator
    public TakeCardCommand(@JsonProperty("cardId") String cardId) {
        this.cardId = cardId;
    }

    @Override
    public void execute(SocketClientHandler handler) {
        try {
            List<GameDelta> deltas = handler.getController().handleTakeCard(handler.getNickname(), cardId);
            List<String> recipients = handler.getController().getPlayersInGame(handler.getNickname());
            for (GameDelta d : deltas) {
                handler.getServer().broadcastToGame(recipients, d);
            }
        } catch (Exception e) {
            try { handler.getSelfView().reportError(e.getMessage()); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    public String getCardId() {
        return cardId;
    }
}
