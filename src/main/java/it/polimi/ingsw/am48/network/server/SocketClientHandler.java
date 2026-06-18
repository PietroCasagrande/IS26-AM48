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
import it.polimi.ingsw.am48.utils.JsonMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Server-side handler for a single Socket client connection.
 *
 * <p>One instance is created per accepted {@link Socket} and run on its own thread
 * (implements {@link Runnable}). It plays two roles:
 * <ol>
 *   <li><b>Command receiver:</b> {@link #run()} continuously reads JSON lines from the
 *       client, deserialises each into a {@link ClientCommand} via Jackson's polymorphic
 *       type resolution, and calls {@link ClientCommand#execute} passing {@code this} as
 *       the handler — giving the command access to {@link #getController()},
 *       {@link #getServer()}, and {@link #getNickname()}.</li>
 *   <li><b>{@link VirtualViewSocket}:</b> the {@code show*}/{@code reportError} methods
 *       wrap their argument in the corresponding {@link ServerNotification} subclass,
 *       serialise it to JSON, and write it to the client's output stream.</li>
 * </ol>
 *
 * <p>When the read loop ends (client disconnects or an {@link IOException} occurs), the
 * {@code finally} block in {@link #run()} performs full cleanup: it terminates the
 * client's game via {@link GameController#handleClientDisconnect}, notifies the remaining
 * players ("companions") that the game has ended, unregisters this client from
 * {@link MesosServer}, and closes the socket.
 */
public class SocketClientHandler implements Runnable, VirtualViewSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private GameController controller;
    private MesosServer server; // server's reference for the broadcast
    private String nickname; // identifies this client

    /**
     * Wraps the given socket and sets up buffered input/output streams for line-based
     * JSON communication.
     *
     * @param socket     the accepted client connection
     * @param controller the game controller used to execute commands received from this client
     * @param server     the shared server hub used for broadcasting and client registration
     * @throws IOException if the socket's input/output streams cannot be obtained
     */
    public SocketClientHandler(Socket socket, GameController controller, MesosServer server) throws IOException {
        this.socket = socket;
        this.controller = controller;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.mapper = JsonMapper.get();
    }

    /**
     * Main read loop for this client's connection, run on a dedicated thread.
     *
     * <p>Each line read from the socket is deserialised into a {@link ClientCommand}
     * (the {@code "type"} field selects the concrete subclass) and executed immediately.
     * The loop ends when the client closes the connection or an {@link IOException} occurs.
     *
     * <p>On exit, if this client had completed a {@code join} (i.e. {@link #nickname} is set),
     * the {@code finally} block tears down the client's game: it calls
     * {@link GameController#handleClientDisconnect} to obtain the remaining players
     * ("companions"), notifies them via {@link MesosServer#broadcastErrorToGame} that the
     * game has been terminated, and unregisters this client from {@link MesosServer}.
     * The socket is then closed regardless of whether a nickname was set.
     */
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
            if(nickname != null) {
                // pick active clients (companions) and clean GameManager
                List<String> companions = controller.handleClientDisconnect(nickname);
                // notify companions about client's disconnection
                server.broadcastErrorToGame(companions,
                        "Player '" + nickname + "' disconnected. The game has been terminated.");
                // Remove this client from the server
                server.unregisterClient(nickname);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Methods used by ClientCommands in order to execute actions ---

    public GameController getController() { return controller; }
    public MesosServer getServer() { return server; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    /**
     * Returns this handler itself as a {@link VirtualView}, so that {@link ClientCommand}
     * implementations can send notifications (e.g. error reports) directly back to this client.
     *
     * @return this instance, viewed as a {@link VirtualView}
     */
    public VirtualView getSelfView() { return this; }


    // --- VirtualViewSocket implementation (sends messages to THE client) ---

    /**
     * {@inheritDoc}
     *
     * <p>Wraps {@code delta} in a {@link GameDeltaNotification} and sends it as a JSON line.
     */
    @Override
    public void showGameDelta(GameDelta delta) {
        send(new GameDeltaNotification(delta));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps {@code snapshot} in an {@link InitialSnapshotNotification} and sends it as
     * a JSON line.
     */
    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) {
        send(new InitialSnapshotNotification(snapshot));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps {@code errorMessage} in an {@link ErrorNotification} and sends it as a JSON line.
     */
    @Override
    public void reportError(String errorMessage) {
        send(new ErrorNotification(errorMessage));    }

    /**
     * Serialises the given notification to JSON and writes it as a single line to the
     * client's output stream.
     *
     * @param notification the notification to send
     */
    private void send(ServerNotification notification) {
        try {
            out.println(mapper.writeValueAsString(notification));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
