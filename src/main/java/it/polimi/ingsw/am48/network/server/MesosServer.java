package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Central, transport-agnostic hub for broadcasting notifications to connected clients.
 *
 * <p>{@code MesosServer} maintains a single map from player nickname to {@link VirtualView},
 * regardless of whether the underlying connection is a Socket
 * ({@link SocketClientHandler}) or RMI ({@link RmiViewAdapter}). Both
 * {@link RmiServer} and {@link SocketClientHandler} register their clients here via
 * {@link #registerClient}, and call the {@code broadcast*} methods after executing a player
 * action so that every relevant client receives the update through whichever transport it
 * is connected with.
 *
 * <p>By design, {@code MesosServer} has no dependency on {@code GameManager} or any other
 * model class: it only knows nicknames and {@link VirtualView} references, keeping the
 * networking layer decoupled from game logic.
 *
 * <p><b>Thread safety:</b> {@code connectedPlayers} is a {@link ConcurrentHashMap}, since
 * multiple client-handling threads (one per Socket connection, plus RMI calls) register,
 * unregister, and read from it concurrently.
 */
public class MesosServer {
    // Map thread-safe per gestire le connessioni concorrenti
    private final Map<String, VirtualView> connectedPlayers = new ConcurrentHashMap<>();

    /**
     * Registers a connected client under the given nickname, making it a valid recipient
     * for subsequent {@code broadcast*} calls.
     *
     * @param nickname the player's nickname, used as the lookup key
     * @param view     the {@link VirtualView} through which notifications will be delivered
     *                  to this client (a {@link SocketClientHandler} or an
     *                  {@link RmiViewAdapter})
     */
    public void registerClient(String nickname, VirtualView view) {
        connectedPlayers.put(nickname, view);
    }

    /**
     * Sends an incremental state update to every recipient in the given list.
     *
     * <p>Recipients not currently registered (e.g. already disconnected) are silently
     * skipped. Delivery failures are currently swallowed; the inline comment marks this
     * as the place where disconnect handling could be triggered if needed.
     *
     * @param recipients the nicknames of the players to notify (typically all players in a game)
     * @param delta      the state update to deliver
     */
    public void broadcastToGame(List<String> recipients, GameDelta delta) {
        for (String nickname : recipients) {
            VirtualView view = connectedPlayers.get(nickname);
            if (view != null) {
                try { view.showGameDelta(delta); }
                catch (Exception e) {
                    /* handle disconnect */
                    // unregisterClient(nickname)
                }
            }
        }
    }

    // overload per GameSnapshot (Scenario 1B, joingame e set up partita, snapshot completo)
    /**
     * Sends a full game snapshot to every recipient in the given list.
     *
     * <p>Used when a game starts (all players receive the initial snapshot at once) and
     * when a player reconnects after a server crash. Recipients not currently registered
     * are silently skipped.
     *
     * @param recipients the nicknames of the players to notify
     * @param snapshot   the full game state to deliver
     */
    public void broadcastSnapshotToGame(List<String> recipients, GameSnapshot snapshot) {
        for (String nickname : recipients) {
            VirtualView view = connectedPlayers.get(nickname);
            if (view != null) {
                try { view.showInitialSnapshot(snapshot); }
                catch (Exception e) {
                    /* handle disconnect */
                    // unregisterClient(nickname)
                }
            }
        }
    }

    /**
     * Sends an error message to every recipient in the given list.
     *
     * <p>Used in particular to notify the remaining players ("companions") when one player
     * disconnects and their shared game is terminated. Recipients not currently registered,
     * or already disconnected themselves, are silently skipped.
     *
     * @param recipients the nicknames of the players to notify
     * @param message    the human-readable error/notification text
     */
    public void broadcastErrorToGame(List<String> recipients, String message) {
        for (String nickname : recipients) {
            VirtualView view = connectedPlayers.get(nickname);
            if(view != null) {
                try { view.reportError(message); }
                catch (Exception e) { /* other clients might already be disconnected */ }
            }
        }
    }

    /**
     * Removes a client's registration, freeing its nickname so that another client can
     * later join using the same nickname.
     *
     * @param nickname the nickname to remove from the registry
     */
    public void unregisterClient(String nickname) {
        connectedPlayers.remove(nickname);
    }
}
