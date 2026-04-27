package it.polimi.ingsw.am48.network.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.VirtualViewSocket;
import it.polimi.ingsw.am48.network.messages.commands.ClientCommand;
import it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification;
import it.polimi.ingsw.am48.network.messages.notifications.GameDeltaNotification;
import it.polimi.ingsw.am48.network.messages.notifications.InitialSnapshotNotification;
import it.polimi.ingsw.am48.network.messages.notifications.ServerNotification;

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
                ClientCommand command = mapper.readValue(line, ClientCommand.class);
                command.execute(this);
            }
        } catch (IOException e) {
            System.err.println("connessione persa con " + nickname);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Metodi usati dai ClientCommand per eseguire le azioni ---

    public GameController getController() { return controller; }
    public MesosServer getServer() { return server; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public VirtualView getSelfView() { return this; }

    public GameController getController() { return controller; }
    public MesosServer getServer() { return server; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public VirtualView getSelfView() { return this; }


    // --- Implementazione VirtualViewSocket (invia messaggi AL client) ---

    @Override
    public void showGameDelta(GameDelta delta) {
        send(new GameDeltaNotification(delta));
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) {
        send(new InitialSnapshotNotification(snapshot));
    }

    @Override
    public void reportError(String errorMessage) {
        send(new ErrorNotification(errorMessage));    }

    private void send(ServerNotification notification) {
        try {
            out.println(mapper.writeValueAsString(notification));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
