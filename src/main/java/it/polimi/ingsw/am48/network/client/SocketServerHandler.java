package it.polimi.ingsw.am48.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.network.VirtualServerSocket;
import it.polimi.ingsw.am48.network.messages.commands.ClientCommand;
import it.polimi.ingsw.am48.network.messages.commands.JoinGameCommand;
import it.polimi.ingsw.am48.network.messages.commands.PlaceTotemCommand;
import it.polimi.ingsw.am48.network.messages.commands.TakeCardCommand;
import it.polimi.ingsw.am48.network.messages.notifications.ServerNotification;
import it.polimi.ingsw.am48.utils.JsonMapper;

import java.io.*;
import java.net.Socket;

/**
 * Handles client-side communication with the server over a TCP socket.
 * Implements {@link VirtualServerSocket} to send commands (join, place
 * totem, take card) as JSON lines, and runs a listener thread that
 * reads incoming {@link ServerNotification} objects and applies them
 * to the local {@link ClientModel}.
 *
 * <p>On connection loss, starts a reconnect watcher thread that polls
 * the server until it comes back online, then instructs the player
 * to rejoin.</p>
 */
public class SocketServerHandler implements Runnable, VirtualServerSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private ClientModel model;
    private final String host;
    private final int port;

    /**
     * Opens a TCP socket to the specified host and port and sets up
     * input/output streams for JSON communication.
     *
     * @param host the server hostname or IP address
     * @param port the server port
     * @param model the {@link ClientModel} to update with received notifications
     * @throws IOException if the socket cannot be opened
     */
    public SocketServerHandler(String host, int port, ClientModel model) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.mapper = JsonMapper.get();
        this.model = model;
    }

    /**
     * Listener thread that continuously reads JSON lines from the server.
     * Each line is deserialized into a {@link ServerNotification} and
     * applied to the {@link ClientModel}. On connection loss, triggers
     * crash recovery.
     */
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

    /**
     * Serializes a {@link ClientCommand} to JSON and sends it over the
     * socket.
     *
     * @param command the command to send
     */
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
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Handles a server crash or connection loss. If the game has not
     * ended, notifies the player and starts a reconnect watcher.
     */
    private void handleServerCrash() {
        if(model.isGameEnded()) return;
        model.notifyError(buildCrashMessage());
        startReconnectWatcher();
    }

    /**
     * Builds a user-facing crash message depending on whether the
     * player's session nickname is known.
     *
     * @return the crash message string
     */
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

    /**
     * Starts a thread that periodically attempts to reconnect
     * to the server. Once the server is reachable, notifies the player
     * with a message containing their session info for rejoin.
     */
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
