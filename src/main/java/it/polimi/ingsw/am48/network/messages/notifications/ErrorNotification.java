package it.polimi.ingsw.am48.network.messages.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class ErrorNotification extends ServerNotification {
    private String message;

    @JsonCreator
    public ErrorNotification(@JsonProperty("error") String message){
        this.message = message;
    }

    @Override
    public void apply(ClientModel model) {
        model.notifyError(message);
    }

    public String getMessage() {
        return message;
    }
}
