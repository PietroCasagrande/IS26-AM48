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
    private final String host;
    private final int port;

    public SocketServerHandler(String host, int port, ClientModel model) throws IOException {
        this.host = host;
        this.port = port;
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
            handleServerCrash();
        } catch (IOException e) {
            handleServerCrash();
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

    @Override
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Rompe il ciclo while(readLine) e fa morire il thread pulitamente
            }
        } catch (IOException e) {
            System.err.println("Errore durante la disconnessione: " + e.getMessage());
        }
    }

    private void handleServerCrash() {
        if(model.isGameEnded()) return;
        model.notifyError(buildCrashMessage());
        startReconnectWatcher();
    }

    private String buildCrashMessage() {
        String nickname = model.getSessionNickname();
        if(nickname == null) {
            return "\n[!] Lost connection to server. Restart the client once the server comes back online.";
        }
        return String.format("""

            ╔══════════════════════════════════════════════════════╗
            ║  [!] LOST SERVER CONNECTION                          ║
            ║                                                      ║
            ║  Wait for the server to get back online...           ║
            ║  We'll notify you once it'll be available.           ║
            ╚══════════════════════════════════════════════════════╝
            """);
    }

    private void startReconnectWatcher() {
        Thread watcher = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    // tries a test-connection
                    new Socket(host, port).close();
                    // server came back ON
                    String nickname = model.getSessionNickname();
                    int numPlayers = model.getSessionNumPlayers();
                    model.notifyError(String.format("""
            
                        ╔══════════════════════════════════════════════════════╗
                        ║  [!] SERVER BACK ONLINE                              ║
                        ║                                                      ║
                        ║  Quit the game, restart the client and type:         ║
                        ║       join %s %d                                     ║
                        ║  to get back in the same game.                       ║
                        ║                                                      ║
                        ╚══════════════════════════════════════════════════════╝
                        """, nickname, numPlayers));
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    // server is still down, try again
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }
}
