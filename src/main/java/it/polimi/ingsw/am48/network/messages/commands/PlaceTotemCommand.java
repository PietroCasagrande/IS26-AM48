package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

/**
 * Command sent by a client to place their totem on a slot of the offer track.
 *
 * <p>On {@link #execute}, delegates to the controller's {@code handlePlaceTotem} method,
 * then broadcasts each resulting {@link GameDelta} to all players in the game.
 * Any game-rule exception (e.g. slot already occupied, wrong phase) is caught and
 * returned to the issuing client as an
 * {@link it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification}.
 */
public class PlaceTotemCommand extends ClientCommand {
    private char position;

    /**
     * Constructs a {@code PlaceTotemCommand}. The {@link JsonCreator} annotation enables
     * Jackson to deserialise this command from a JSON payload received over the Socket.
     *
     * @param position the offer-track slot on which the totem should be placed (A–G)
     */
    @JsonCreator
    public PlaceTotemCommand(@JsonProperty("position") char position) {
        this.position = position;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invokes {@code handlePlaceTotem} on the controller and broadcasts the resulting
     * deltas to all players currently in the game.
     */
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

    public char getPosition() {
        return position;
    }
}
