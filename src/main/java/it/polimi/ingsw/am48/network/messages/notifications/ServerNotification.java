package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * Abstract base class for all messages sent from the server to clients over the Socket protocol.
 *
 * <p>Each concrete subclass wraps a specific server-side event — an incremental state update,
 * an initial full snapshot, or an error — and knows how to apply itself to the client's local
 * {@link ClientModel}. The Visitor-like {@link #apply} method keeps the client's receive loop
 * simple: it deserialises the incoming JSON into a {@code ServerNotification} and calls
 * {@code apply(model)} without needing to branch on the message type.
 *
 * <p>Jackson polymorphic deserialization is configured via {@link JsonTypeInfo} and
 * {@link JsonSubTypes}: the {@code "type"} field in the JSON payload selects the correct
 * concrete class automatically.
 *
 * <p>Together with {@link it.polimi.ingsw.am48.network.messages.commands.ClientCommand},
 * this class forms the two-way Socket messaging protocol:
 * commands flow <b>client → server</b>, notifications flow <b>server → client</b>.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameDeltaNotification.class, name = "gameDelta"),
        @JsonSubTypes.Type(value = InitialSnapshotNotification.class, name = "initialSnapshot"),
        @JsonSubTypes.Type(value = ErrorNotification.class, name = "error")
})
public abstract class ServerNotification {

    /**
     * Applies this notification to the given client model.
     *
     * <p>Implementations update the local game state (e.g. applying a delta, replacing the
     * state with a full snapshot, or propagating an error) and then rely on the registered
     * {@link it.polimi.ingsw.am48.network.client.ModelObserver} instances to refresh the view.
     *
     * @param model the local client model to update
     */
    public abstract void apply(ClientModel model);
}