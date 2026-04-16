package it.polimi.ingsw.am48.network.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServerSocket;
import it.polimi.ingsw.am48.network.messages.NetworkMessage;

import java.io.*;
import java.net.Socket;

public class SocketServerHandler implements Runnable, VirtualServerSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private ClientModel model; // Per aggiornare lo stato locale

    public SocketServerHandler(String host, int port, ClientModel model) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.mapper = new ObjectMapper();
        this.model = model;
    }

    // Thread in ascolto dei messaggi dal SERVER
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                NetworkMessage msg = mapper.readValue(line, NetworkMessage.class);
                switch (msg.getType()) {
                    case "gameDelta" -> model.applyDelta(mapper.treeToValue(msg.getPayload(), GameDelta.class));
                    case "initialSnapshot" -> model.setInitialState(mapper.treeToValue(msg.getPayload(), GameSnapshot.class));
                    case "error" -> model.notifyError(msg.getPayload().asText());
                }
            }
            // qui connessione persa in modo pulito
            model.notifyError("connessione al server persa.");
        } catch (IOException e) {
            // qui invece se l'ha persa in modo brusco
            model.notifyError("connessione al server persa.");
        }
    }

    @Override
    public void joinGame(int numPlayers, String nickname) {
        var payload = mapper.createObjectNode();
        payload.put("numPlayers", numPlayers);
        payload.put("nickname", nickname);
        sendMessage("joinGame", payload);
    }

    @Override
    public void placeTotem(String nickname, char position) {
        var payload = mapper.createObjectNode();
        payload.put("nickname", nickname);
        payload.put("position", String.valueOf(position));
        sendMessage("placeTotem", payload);
    }

    @Override
    public void takeCard(String nickname, String cardId) {
        var payload = mapper.createObjectNode();
        payload.put("nickname", nickname);
        payload.put("cardId", cardId);
        sendMessage("takeCard", payload);
    }

    private void sendMessage(String type, JsonNode payload) {
        try {
            NetworkMessage msg = new NetworkMessage(type, payload);
            out.println(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
