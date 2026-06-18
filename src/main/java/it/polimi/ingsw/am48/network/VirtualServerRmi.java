package it.polimi.ingsw.am48.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI-specific extension of {@link VirtualServer}, exposed by the server as a remote object.
 *
 * <p>Extends {@link Remote} (required for any RMI remote interface) and re-declares the
 * methods of {@link VirtualServer} to throw {@link RemoteException}, as mandated by RMI for
 * every method of a remote interface. Two additional methods are introduced that have no
 * equivalent on the Socket side:
 * <ul>
 *   <li>{@link #connect}: registers the client's callback object with the server, so the
 *       server can later push notifications back to this client.</li>
 *   <li>{@link #ping}: used by the client's heartbeat thread to detect a server crash.</li>
 * </ul>
 *
 * @see VirtualServer
 * @see VirtualServerSocket
 * @see it.polimi.ingsw.am48.network.server.RmiServer
 */
public interface VirtualServerRmi extends Remote, VirtualServer{

    /**
     * Registers the calling client's RMI callback object with the server, so that
     * subsequent game notifications (deltas, snapshots, errors) can be pushed to it.
     *
     * <p>Must be called once, before any other command, immediately after the client
     * has been exported as a remote object.
     *
     * @param nickname the nickname the client intends to use for this session
     * @param client   the client's own remote callback object, used by the server to
     *                 invoke {@link VirtualViewRmi} methods on this client
     * @throws RemoteException if the RMI call fails
     */
    void connect(String nickname, VirtualViewRmi client) throws RemoteException;

    /** {@inheritDoc} */
    @Override void joinGame(int numPlayers, String nickname) throws RemoteException;

    /** {@inheritDoc} */
    @Override void placeTotem(String nickname, char position) throws RemoteException;

    /** {@inheritDoc} */
    @Override void takeCard(String nickname, String cardId) throws RemoteException;

    /**
     * No-op method used as a liveness probe.
     *
     * <p>If the server is reachable, this call returns normally; if the server process
     * has crashed or is unreachable, RMI raises a {@link RemoteException}, which the
     * caller's heartbeat thread interprets as a server crash.
     *
     * @throws RemoteException if the server is unreachable
     */
    void ping() throws RemoteException;
}
