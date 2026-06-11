package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * Server notification carrying an error message to be displayed to the client.
 *
 * <p>Sent when the server rejects a command due to a game-rule violation or an invalid
 * argument (e.g. a duplicate nickname, an illegal totem position, or attempting to take
 * a card outside the allowed phase). On {@link #apply}, delegates to
 * {@link ClientModel#notifyError}, which propagates the message to all registered
 * {@link it.polimi.ingsw.am48.network.client.ModelObserver} instances.
 */
public class ErrorNotification extends ServerNotification {
    private String message;

    /**
     * Constructs an {@code ErrorNotification}. The {@link JsonCreator} annotation enables
     * Jackson to deserialise this notification from a JSON payload received over the Socket.
     *
     * @param message the human-readable error description produced by the server
     */
    @JsonCreator
    public ErrorNotification(@JsonProperty("message") String message){
        this.message = message;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link ClientModel#notifyError} with the error message,
     * causing the view to display it to the user.
     */
    @Override
    public void apply(ClientModel model) {
        model.notifyError(message);
    }

    public String getMessage() {
        return message;
    }
}
