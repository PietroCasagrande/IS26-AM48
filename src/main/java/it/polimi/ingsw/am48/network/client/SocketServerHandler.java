package it.polimi.ingsw.am48.network.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServerSocket;
import it.polimi.ingsw.am48.network.messages.commands.ClientCommand;
import it.polimi.ingsw.am48.network.messages.commands.JoinGameCommand;
import it.polimi.ingsw.am48.network.messages.commands.PlaceTotemCommand;
import it.polimi.ingsw.am48.network.messages.commands.TakeCardCommand;
import it.polimi.ingsw.am48.network.messages.notifications.ServerNotification;
import it.polimi.ingsw.am48.utils.JsonMapper;

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
        this.mapper = JsonMapper.get();
        this.model = model;
    }

    // Thread in ascolto delle notifiche dal SERVER
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                ServerNotification notification = mapper.readValue(line, ServerNotification.class);
                notification.apply(model);
            }
            model.notifyError("Connessione al server persa.");
        } catch (IOException e) {
            e.printStackTrace();
            model.notifyError("Connessione al server persa.");
        }
    }

    @Override
    public void joinGame(int numPlayers, String nickname) {
        send(new JoinGameCommand(numPlayers, nickname));
    }

    @Override
    public void placeTotem(String nickname, char position) {
        send(new PlaceTotemCommand(position));
    }

    @Override
    public void takeCard(String nickname, String cardId) {
        send(new TakeCardCommand(cardId));
    }

    private void send(ClientCommand command) {
        try {
            out.println(mapper.writeValueAsString(command));
        } catch (Exception e) {
            System.err.println("SEND ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
