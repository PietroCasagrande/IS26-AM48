package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

/**
 * Command sent by a client to join or create a game session.
 *
 * <p>On {@link #execute}, this command performs three steps in sequence:
 * <ol>
 *   <li>Registers the client's nickname and view reference on the server, so that
 *       subsequent broadcasts can reach this connection.</li>
 *   <li>Delegates to the controller's {@code handleJoinGame} method, which either adds
 *       the player to an existing waiting lobby or creates a new one.</li>
 *   <li>If the lobby is now full and the game has started, broadcasts the initial snapshot
 *       to <em>all</em> players in the game. Otherwise, sends it only to the joining client
 *       so it can display the waiting screen until the remaining players join.</li>
 * </ol>
 * Game-rule exceptions (e.g. duplicate nickname) are caught and returned to the sender
 * as an {@link it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification}.
 */
public class JoinGameCommand extends ClientCommand{
    private int numPlayers;
    private String nickname;

    /**
     * Constructs a {@code JoinGameCommand}. The {@link JsonCreator} annotation enables
     * Jackson to deserialise this command from a JSON payload received over the Socket.
     *
     * @param numPlayers the desired game size (number of players to wait for)
     * @param nickname   the player's chosen display name
     */
    @JsonCreator
    public JoinGameCommand(
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("nickname") String nickname) {
        this.numPlayers = numPlayers;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers the client, delegates to the controller, and broadcasts the initial
     * snapshot either to all players (game started) or only to this client (still waiting).
     */
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

    public int getNumPlayers() {
        return numPlayers;
    }

    public String getNickname() {
        return nickname;
    }
}
