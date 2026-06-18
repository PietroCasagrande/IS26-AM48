package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side RMI endpoint, exported as the remote object {@code "MesosServer"} and
 * looked up by every {@link it.polimi.ingsw.am48.network.client.RmiClient}.
 *
 * <p>This class implements {@link VirtualServerRmi}: each method receives a remote call
 * from a client, delegates the actual game logic to {@link GameController}, and forwards
 * the resulting deltas or snapshots to the relevant clients via {@link MesosServer}'s
 * broadcast methods. Game-rule exceptions are caught and reported back only to the
 * originating client through its registered {@link VirtualViewRmi} callback.
 *
 * <p>{@code rmiCallbacks} maps each connected player's nickname to their remote callback
 * object, registered via {@link #connect}. This map is used to look up where to send
 * {@code reportError} and {@code showInitialSnapshot} replies that must go only to the
 * calling client (as opposed to deltas, which are broadcast to the whole game via
 * {@link MesosServer}).
 */
public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {
    private final GameController controller;
    private final MesosServer mesosServer;
    private final Map<String, VirtualViewRmi> rmiCallbacks = new ConcurrentHashMap<>();

    /**
     * Constructs the RMI server endpoint and exports it as a remote object.
     *
     * @param controller  the game controller used to execute player actions
     * @param mesosServer the shared server-side hub used to register clients and
     *                     broadcast notifications across both transports
     * @throws RemoteException if exporting this object fails
     */
    public RmiServer(GameController controller, MesosServer mesosServer) throws RemoteException {
        super();
        this.controller = controller;
        this.mesosServer = mesosServer;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers the client's remote callback under its nickname in {@code rmiCallbacks},
     * then wraps it in a {@link RmiViewAdapter} — which starts its own heartbeat against
     * this client — and registers that adapter as the client's {@link VirtualView} with
     * {@link MesosServer}.
     *
     * <p>The {@code onDisconnect} callback passed to the adapter is invoked by the adapter's
     * heartbeat when this client stops responding. When triggered, it removes the client
     * from {@code rmiCallbacks}, terminates the client's game via
     * {@link GameController#handleClientDisconnect}, notifies the remaining players
     * ("companions") that the game has ended, and unregisters the client from
     * {@link MesosServer}.
     */
    @Override
    public void connect(String nickname, VirtualViewRmi client) throws RemoteException{
        rmiCallbacks.put(nickname, client);

        // this thread is executed each time RmiViewAdapter detects a disconnection
        Runnable onDisconnect = () -> {
            rmiCallbacks.remove(nickname);
            List<String> companions = controller.handleClientDisconnect(nickname);
            mesosServer.broadcastErrorToGame(companions,
                    "Player '" + nickname + "' disconnected. The game has been terminated.");
            mesosServer.unregisterClient(nickname);
        };

        VirtualView viewAdapter = new RmiViewAdapter(client, onDisconnect);
        mesosServer.registerClient(nickname, viewAdapter);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link GameController#handleJoinGame}. If the join completes the
     * lobby, the initial snapshot is broadcast to every player in the game via
     * {@link MesosServer#broadcastSnapshotToGame}; otherwise it is sent only to this
     * client through its {@code rmiCallbacks} entry. Any exception is reported back to
     * the calling client only.
     */
    @Override
    public void joinGame(int numPlayers, String nickname) throws RemoteException {
        try {
            JoinResult result = controller.handleJoinGame(numPlayers, nickname);
            if (result.gameStarted()) {
                List<String> recipients = controller.getPlayersInGame(nickname);
                mesosServer.broadcastSnapshotToGame(recipients, result.snapshot());
            } else {
                rmiCallbacks.get(nickname).showInitialSnapshot(result.snapshot());
            }
        } catch (Exception e) {
            try{rmiCallbacks.get(nickname).reportError(e.getMessage()); }
            catch (RemoteException ex) {ex.printStackTrace();}
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link GameController#handlePlaceTotem} and broadcasts every
     * resulting {@link GameDelta} to all players in the game via
     * {@link MesosServer#broadcastToGame}. Any exception is reported back to the
     * calling client only.
     */
    @Override
    public void placeTotem(String nickname, char position) throws RemoteException {
        try {
            List<GameDelta> deltas = controller.handlePlaceTotem(nickname, position);
            List<String> recipients = controller.getPlayersInGame(nickname);
            for(GameDelta d : deltas){
                mesosServer.broadcastToGame(recipients, d);
            }
        } catch (Exception e) {
            try { rmiCallbacks.get(nickname).reportError(e.getMessage());}
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link GameController#handleTakeCard} and broadcasts every
     * resulting {@link GameDelta} to all players in the game via
     * {@link MesosServer#broadcastToGame}. If this move ends the game, the delta list
     * transparently includes a trailing {@code LeaderboardDelta}, which is broadcast like
     * any other delta. Any exception is reported back to the calling client only.
     */
    @Override
    public void takeCard(String nickname, String cardId) throws RemoteException {
        try {
            List<GameDelta> deltas = controller.handleTakeCard(nickname, cardId);
            List<String> recipients = controller.getPlayersInGame(nickname);
            for (GameDelta delta : deltas) {
                mesosServer.broadcastToGame(recipients, delta);
            }
        } catch (Exception e) {
            try { rmiCallbacks.get(nickname).reportError(e.getMessage());}
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>No-op: the mere fact that this remote call succeeds tells a client's heartbeat
     * thread that the server is still alive.
     */
    @Override
    public void ping() throws RemoteException {
        // empty method: if the server's alive, it answers. Otherwise, RemoteException.
    }
}
