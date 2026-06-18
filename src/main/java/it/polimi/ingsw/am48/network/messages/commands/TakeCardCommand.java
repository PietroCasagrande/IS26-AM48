package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

import java.util.List;

/**
 * Command sent by a client to take a card from the showed rows.
 *
 * <p>On {@link #execute}, delegates to the controller's {@code handleTakeCard} method,
 * then broadcasts each resulting {@link GameDelta} to all players in the game.
 * If the move ends the game, the delta list transparently includes a
 * {@link it.polimi.ingsw.am48.model.delta.LeaderboardDelta} appended by
 * {@link it.polimi.ingsw.am48.model.game.GameManager#takeCard}, which is broadcast
 * to all players along with the other deltas without any special handling here.
 * Any game-rule exception is caught and returned to the issuing client as an
 * {@link it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification}.
 */
public class TakeCardCommand extends ClientCommand {
    private String cardId;

    /**
     * Constructs a {@code TakeCardCommand}. The {@link JsonCreator} annotation enables
     * Jackson to deserialise this command from a JSON payload received over the Socket.
     *
     * @param cardId the identifier of the card the player wants to take (e.g. {@code "HUN_1"})
     */
    @JsonCreator
    public TakeCardCommand(@JsonProperty("cardId") String cardId) {
        this.cardId = cardId;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invokes {@code handleTakeCard} on the controller and broadcasts the resulting
     * deltas to all players currently in the game. Any trailing {@code LeaderboardDelta}
     * produced at game end is included in the broadcast automatically.
     */
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
