package it.polimi.ingsw.am48.network.messages;

import com.fasterxml.jackson.databind.JsonNode;

public class NetworkMessage {
    private String type; // considerate tipo "joinGame", "gameDelta", "error"
    private JsonNode payload; // Il contenuto dinamico

    // costruttori, getter e setter per Jackson
    public NetworkMessage() {}

    public NetworkMessage(String type, JsonNode payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}