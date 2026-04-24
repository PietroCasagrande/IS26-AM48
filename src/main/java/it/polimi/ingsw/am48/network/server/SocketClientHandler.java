package it.polimi.ingsw.am48.network.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualViewSocket;
import it.polimi.ingsw.am48.network.messages.NetworkMessage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SocketClientHandler implements Runnable, VirtualViewSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private GameController controller;
    private MesosServer server; // Riferimento al server principale per il broadcast
    private String nickname; // Identifica questo client

    public SocketClientHandler(Socket socket, GameController controller, MesosServer server) throws IOException {
        this.socket = socket;
        this.controller = controller;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.mapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                NetworkMessage msg = mapper.readValue(line, NetworkMessage.class);
                handleMessage(msg);
            }
        } catch (IOException e) {
            System.err.println("connessione persa con " + nickname);

            // da gestire disconnessione

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(NetworkMessage msg) {
        try {
            switch (msg.getType()) {
                case "joinGame":
                    int numPlayers = msg.getPayload().get("numPlayers").asInt();
                    this.nickname = msg.getPayload().get("nickname").asText();
                    server.registerClient(nickname, this); // Registra la view

                    JoinResult result = controller.handleJoinGame(numPlayers, nickname);
                    if (result.gameStarted()) {
                        // Scenario join game e set up game (1B): broadcast a tutti i player della partita
                        List<String> recipients = controller.getPlayersInGame(nickname);
                        server.broadcastSnapshotToGame(recipients, result.snapshot());
                    } else {
                        // Scenario snapshot incompleto (1A): solo a questo client
                        showInitialSnapshot(result.snapshot());
                    }
                    break;

                case "placeTotem":
                    char pos = msg.getPayload().get("position").asText().charAt(0);
                    List<GameDelta> placeTotemDeltas = controller.handlePlaceTotem(this.nickname, pos);
                    List<String> recipientsTotem = controller.getPlayersInGame(this.nickname);
                    for (GameDelta d : placeTotemDeltas) {
                        server.broadcastToGame(recipientsTotem, d);
                    }
                    break;

                case "takeCard":
                    String cardId = msg.getPayload().get("cardId").asText();
                    List<GameDelta> playerOfferDeltas = controller.handleTakeCard(this.nickname, cardId);
                    List<String> recipientsCard = controller.getPlayersInGame(this.nickname);
                    for (GameDelta d : playerOfferDeltas) {
                        server.broadcastToGame(recipientsCard, d);
                    }
                    break;
            }
        } catch (Exception e) {
            try {
                reportError(e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- Implementazione VirtualViewSocket (invia messaggi AL client) ---

    @Override
    public void showGameDelta(GameDelta delta) {
        sendMessage("gameDelta", delta);
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) {
        sendMessage("initialSnapshot", snapshot);
    }

    @Override
    public void reportError(String errorMessage) {
        sendMessage("error", errorMessage); // Crea un piccolo record/oggetto per l'errore se serve
    }

    private void sendMessage(String type, Object payloadData) {
        try {
            NetworkMessage msg = new NetworkMessage(type, mapper.valueToTree(payloadData));
            out.println(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
