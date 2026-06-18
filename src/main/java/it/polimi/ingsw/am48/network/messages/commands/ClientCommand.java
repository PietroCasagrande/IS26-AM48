package it.polimi.ingsw.am48.network.messages.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

/**
 * Abstract base class for all messages sent from a client to the server over the Socket protocol.
 *
 * <p>Each concrete subclass represents a specific player action (join, place totem, take card)
 * and carries the data needed to carry it out. The Command pattern is applied: once a JSON
 * message is received and deserialised by the server, the resulting object is self-contained
 * and executes its action entirely within {@link #execute}.
 *
 * <p>Jackson polymorphic deserialization is configured via {@link JsonTypeInfo} and
 * {@link JsonSubTypes}: the {@code "type"} field in the JSON payload selects the correct
 * concrete class, so the server's read loop can deserialise any incoming message with a
 * single {@code readValue(ClientCommand.class)} call without inspecting the content manually.
 *
 * <p>Together with {@link it.polimi.ingsw.am48.network.messages.notifications.ServerNotification},
 * this class forms the two-way Socket messaging protocol:
 * commands flow <b>client → server</b>, notifications flow <b>server → client</b>.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinGameCommand.class, name = "joinGame"),
        @JsonSubTypes.Type(value = PlaceTotemCommand.class, name = "placeTotem"),
        @JsonSubTypes.Type(value = TakeCardCommand.class, name = "takeCard")
})
public abstract class ClientCommand {

    /**
     * Executes this command in the context of the given client handler.
     *
     * <p>Implementations are responsible for invoking the appropriate controller method,
     * collecting the resulting deltas or join result, and broadcasting the response to the
     * relevant clients via the server reference held by {@code handler}.
     * Game-rule exceptions must be caught and reported back to the sender as an
     * {@link it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification}.
     *
     * @param handler the handler representing the client connection that sent this command
     */
    public abstract void execute(SocketClientHandler handler);
}