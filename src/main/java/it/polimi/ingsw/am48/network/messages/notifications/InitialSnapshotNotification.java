package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * Server notification carrying the full initial game snapshot.
 *
 * <p>Sent in two scenarios:
 * <ul>
 *   <li><b>Game start:</b> broadcast to all players at once when the lobby reaches
 *       its target size, replacing any partial lobby state on the client.</li>
 *   <li><b>Reconnect:</b> sent individually to a reconnecting player so they can
 *       rebuild their local state from the current server snapshot and resume play.</li>
 * </ul>
 * On {@link #apply}, delegates to {@link ClientModel#setInitialState}, which replaces
 * the local state entirely and notifies all registered
 * {@link it.polimi.ingsw.am48.network.client.ModelObserver} instances.
 */
public class InitialSnapshotNotification extends ServerNotification {
    private GameSnapshot snapshot;

    /**
     * Constructs an {@code InitialSnapshotNotification}. The {@link JsonCreator} annotation
     * enables Jackson to deserialise this notification from a JSON payload received over the Socket.
     *
     * @param snapshot the full game snapshot to deliver to the client
     */
    @JsonCreator
    public InitialSnapshotNotification(@JsonProperty("initialSnapshot") GameSnapshot snapshot){
        this.snapshot = snapshot;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link ClientModel#setInitialState}, replacing the client's local
     * state with the received snapshot and triggering a full view refresh.
     */
    @Override
    public void apply(ClientModel model) {
        model.setInitialState(snapshot);
    }

    public GameSnapshot getSnapshot() {
        return snapshot;
    }
}
