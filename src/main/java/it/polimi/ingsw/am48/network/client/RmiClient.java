package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Client-side RMI endpoint, playing two roles at once:
 * <ol>
 *   <li><b>Server proxy</b> ({@link VirtualServer}): the client calls
 *       {@link #joinGame}, {@link #placeTotem}, and {@link #takeCard} on this object,
 *       which forward to the remote {@link VirtualServerRmi} stub looked up in the
 *       RMI registry.</li>
 *   <li><b>Remote callback target</b> ({@link VirtualViewRmi}): by extending
 *       {@link UnicastRemoteObject}, this instance is itself exported as a remote object,
 *       so the server can invoke {@link #showGameDelta}, {@link #showInitialSnapshot}, and
 *       {@link #reportError} on it to push updates.</li>
 * </ol>
 *
 * <p>On construction, a background heartbeat thread is started (see {@link #startHeartbeat}),
 * which periodically pings the server to detect a crash and, if one occurs, starts a
 * reconnect watcher that polls the RMI registry until the server comes back online.
 */
public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, VirtualServer {

    private final VirtualServerRmi server;
    private final ClientModel model;

    private final String host;
    private final int port;

    /**
     * Looks up the {@code "MesosServer"} remote object in the RMI registry at
     * {@code host:port}, exports this client as a remote object, and starts the
     * heartbeat thread.
     *
     * @param host  the hostname or IP address of the RMI registry
     * @param port  the port of the RMI registry
     * @param model the local client model that callback methods will update
     * @throws RemoteException   if exporting this object or contacting the registry fails
     * @throws NotBoundException if no object named {@code "MesosServer"} is bound in the registry
     */
    public RmiClient(String host, int port, ClientModel model) throws RemoteException, NotBoundException {
        super();
        this.host = host;
        this.port = port;
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.server = (VirtualServerRmi) registry.lookup("MesosServer");
        this.model = model;
        startHeartbeat();
    }

    /**
     * Package-private constructor used by {@code RmiClientTest} to test this class's logic
     * in isolation from the RMI registry and network.
     *
     * <p>{@code host} and {@code port} are left unset and the heartbeat thread is
     * <em>not</em> started, since the reconnect watcher relies on a real registry lookup
     * that would not make sense in a unit test.
     *
     * @param server a (possibly mocked) {@link VirtualServerRmi} stub
     * @param model  the local client model that callback methods will update
     * @throws RemoteException if exporting this object fails
     */
    RmiClient(VirtualServerRmi server, ClientModel model) throws RemoteException {
        super();
        this.server = server;
        this.model = model;
        this.host = null;
        this.port = -1;
    }

    // =========================================================================
    // VirtualServer — commands sent from the client to the server
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Before joining, registers this client's remote callback object with the server
     * via {@link VirtualServerRmi#connect}, so that subsequent notifications can be
     * pushed back to it.
     */
    @Override
    public void joinGame(int numPlayers, String nickname) throws Exception{
        server.connect(nickname, this);
        server.joinGame(numPlayers, nickname);
    }

    /** {@inheritDoc} */
    @Override
    public void placeTotem(String nickname, char position) throws Exception{
        server.placeTotem(nickname, position);
    }

    /** {@inheritDoc} */
    @Override
    public void takeCard(String nickname, String cardId) throws Exception{
        server.takeCard(nickname, cardId);
    }

    // =========================================================================
    // VirtualView — callbacks invoked remotely by the server
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Invoked remotely by the server. Delegates to {@link ClientModel#applyDelta}.
     */
    @Override
    public void showGameDelta(GameDelta delta) throws RemoteException{
        model.applyDelta(delta);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invoked remotely by the server. Delegates to {@link ClientModel#setInitialState}.
     */
    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) throws RemoteException{
        model.setInitialState(snapshot);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invoked remotely by the server. Delegates to {@link ClientModel#notifyError}.
     */
    @Override
    public void reportError(String errorMessage) throws RemoteException {
        model.notifyError(errorMessage);
    }

    /**
     * {@inheritDoc}
     *
     * <p>No-op: the mere fact that this remote call succeeds tells the server's
     * {@code RmiViewAdapter} heartbeat that this client is still alive.
     */
    @Override
    public void ping() throws RemoteException {
        // Server pings clients periodically in order to see if they're "still alive".
    }

    // =========================================================================
    // Heartbeat and reconnection
    // =========================================================================

    /**
     * Starts a daemon thread that pings the server every 3 seconds via
     * {@link VirtualServerRmi#ping}.
     *
     * <p>If a ping throws {@link RemoteException}, the server is considered crashed and
     * {@link #handleServerCrash()} is invoked; the thread then terminates. If the thread
     * is interrupted (e.g. on client shutdown), it exits cleanly without further action.
     */
    private void startHeartbeat(){
        Thread heartbeat = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    server.ping();
                } catch (RemoteException e) {
                    handleServerCrash();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        heartbeat.setDaemon(true);
        heartbeat.start();
    }

    /**
     * Reacts to a detected server crash.
     *
     * <p>If the game has already ended normally ({@link ClientModel#isGameEnded()}), the
     * crash is ignored — the heartbeat failing after a clean shutdown is expected and should
     * not trigger a false crash alert. Otherwise, notifies the view with a crash message
     * (using the same wording as the Socket client, for a unified user experience) and
     * starts {@link #startReconnectWatcher()} to detect when the server comes back.
     */
    private void handleServerCrash() {
        if(model.isGameEnded()) return;
        // Same message of SocketServerHandler - unified behaviour
        model.notifyError(buildCrashMessage());
        startReconnectWatcher();
    }

    /**
     * Builds the message shown to the user when the server connection is lost.
     *
     * <p>If the local session has no nickname yet (the player had not joined a game),
     * a generic message is returned. Otherwise, a boxed message instructs the user to
     * wait for the server to return.
     *
     * @return the crash message to display
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
     * Starts a daemon thread that polls the RMI registry every 5 seconds until the
     * {@code "MesosServer"} object becomes reachable again.
     *
     * <p>Once the lookup succeeds, the server is considered back online: the user is
     * shown a message instructing them to restart the client and rejoin the same game
     * using {@code join <nickname> <numPlayers>}, then the watcher thread terminates.
     * Lookup failures while the server is still down are silently ignored and retried.
     */
    private void startReconnectWatcher() {
        Thread watcher = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    LocateRegistry.getRegistry(host, port).lookup("MesosServer");
                    // Server came back ON
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
                } catch (Exception e) {
                    // server is still down, try again
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }
}
