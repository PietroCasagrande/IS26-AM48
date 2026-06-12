package it.polimi.ingsw.am48.network;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

/**
 * Technology-agnostic interface representing a single client, as seen from the server side.
 *
 * <p>This interface decouples the server's broadcast logic from the underlying transport:
 * {@link it.polimi.ingsw.am48.network.server.MesosServer} holds one {@code VirtualView} per
 * connected client and calls these methods to push updates, without knowing whether the
 * client is connected via Socket or RMI.
 *
 * <p>Concrete implementations:
 * <ul>
 *   <li>RMI: {@link it.polimi.ingsw.am48.network.server.RmiViewAdapter}, which wraps a
 *       {@link VirtualViewRmi} remote callback.</li>
 *   <li>Socket: the server-side socket handler's view counterpart.</li>
 * </ul>
 *
 * @see VirtualViewRmi
 * @see VirtualViewSocket
 * @see VirtualServer
 */
public interface VirtualView {

    /**
     * Pushes an incremental state update to this client.
     *
     * @param delta the change to apply to the client's local game state
     * @throws Exception if the notification cannot be delivered (e.g. the client has disconnected)
     */
    void showGameDelta(GameDelta delta) throws Exception;

    /**
     * Pushes a full game snapshot to this client, replacing its local state entirely.
     *
     * <p>Used both when a game starts (broadcast to all players) and when a player
     * reconnects after a server crash (sent individually).
     *
     * @param snapshot the full game state to deliver
     * @throws Exception if the notification cannot be delivered
     */
    void showInitialSnapshot(GameSnapshot snapshot) throws Exception;

    /**
     * Pushes an error message to this client, to be displayed by the view.
     *
     * @param errorMessage the human-readable error description
     * @throws Exception if the notification cannot be delivered
     */
    void reportError(String errorMessage) throws Exception;
}
