package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.client.ClientModel;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameDeltaNotification.class, name = "gameDelta"),
        @JsonSubTypes.Type(value = InitialSnapshotNotification.class, name = "initialSnapshot"),
        @JsonSubTypes.Type(value = ErrorNotification.class, name = "error")
})
public abstract class ServerNotification {
    public abstract void apply(ClientModel model);
}