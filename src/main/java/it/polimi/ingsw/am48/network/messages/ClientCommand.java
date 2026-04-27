package it.polimi.ingsw.am48.network.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinGameCommand.class, name = "joinGame"),
        @JsonSubTypes.Type(value = PlaceTotemCommand.class, name = "placeTotem"),
        @JsonSubTypes.Type(value = TakeCardCommand.class, name = "takeCard")
})
public abstract class ClientCommand {
    public abstract void execute(SocketClientHandler handler);
}