package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class InitialSnapshotNotification extends ServerNotification {
    private GameSnapshot snapshot;

    @JsonCreator
    public InitialSnapshotNotification(@JsonProperty("initialSnapshot") GameSnapshot snapshot){
        this.snapshot = snapshot;
    }

    @Override
    public void apply(ClientModel model) {
        model.setInitialState(snapshot);
    }

    public GameSnapshot getSnapshot() {
        return snapshot;
    }
}
