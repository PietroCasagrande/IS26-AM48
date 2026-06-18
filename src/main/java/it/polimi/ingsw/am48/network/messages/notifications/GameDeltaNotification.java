package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * Server notification carrying an incremental state update ({@link GameDelta}).
 *
 * <p>Sent after every valid player action (place totem, take card). Each delta carries
 * only the fields that changed since the last update, keeping the network payload small
 * compared to a full snapshot. On {@link #apply}, delegates to
 * {@link ClientModel#applyDelta}, which merges the delta into the local state and notifies
 * all registered {@link it.polimi.ingsw.am48.network.client.ModelObserver} instances so
 * that the view refreshes automatically.
 */
public class GameDeltaNotification extends ServerNotification {
    private GameDelta delta;

    /**
     * Constructs a {@code GameDeltaNotification}. The {@link JsonCreator} annotation enables
     * Jackson to deserialise this notification from a JSON payload received over the Socket.
     *
     * @param delta the incremental state update to deliver to the client
     */
    @JsonCreator
    public GameDeltaNotification(@JsonProperty("delta") GameDelta delta){
        this.delta = delta;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link ClientModel#applyDelta}, merging the carried delta into the
     * client's local state and triggering a view refresh via the registered observers.
     */
    @Override
    public void apply(ClientModel model) {
        model.applyDelta(delta);
    }

    public GameDelta getDelta() {
        return delta;
    }
}
