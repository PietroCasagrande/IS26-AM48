package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class GameDeltaNotification extends ServerNotification {
    private GameDelta delta;

    @JsonCreator
    public GameDeltaNotification(@JsonProperty("delta") GameDelta delta){
        this.delta = delta;
    }

    @Override
    public void apply(ClientModel model) {
        model.applyDelta(delta);
    }

    public GameDelta getDelta() {
        return delta;
    }
}
