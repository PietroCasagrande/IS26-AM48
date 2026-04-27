package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

public class JoinGameCommand extends ClientCommand{
    private int numPlayers;
    private String nickname;

    @JsonCreator
    public JoinGameCommand(
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("nickname") String nickname) {
        this.numPlayers = numPlayers;
        this.nickname = nickname;
    }

    @Override
    public void execute(SocketClientHandler handler) {
        handler.setNickname(nickname);
        handler.getServer().registerClient(nickname, handler.getSelfView());
        try {
            JoinResult result = handler.getController().handleJoinGame(numPlayers, nickname);
            if (result.gameStarted()) {
                List<String> recipients = handler.getController().getPlayersInGame(nickname);
                handler.getServer().broadcastSnapshotToGame(recipients, result.snapshot());
            } else {
                handler.getSelfView().showInitialSnapshot(result.snapshot());
            }
        } catch (Exception e) {
            try { handler.getSelfView().reportError(e.getMessage()); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
