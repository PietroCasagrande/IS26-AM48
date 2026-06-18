package it.polimi.ingsw.am48.network;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI-specific extension of {@link VirtualView}, exposed by each client as a remote object.
 *
 * <p>Extends {@link Remote} (required for any RMI remote interface) and re-declares the
 * methods of {@link VirtualView} to throw {@link RemoteException}, as mandated by RMI for
 * every method of a remote interface. One additional method, {@link #ping}, has no
 * equivalent on the Socket side.
 *
 * <p>Implemented by {@link it.polimi.ingsw.am48.network.client.RmiClient}, which exports
 * itself as a {@code UnicastRemoteObject} so the server can invoke these methods as callbacks.
 *
 * @see VirtualView
 * @see VirtualViewSocket
 * @see it.polimi.ingsw.am48.network.client.RmiClient
 * @see it.polimi.ingsw.am48.network.server.RmiViewAdapter
 */
public interface VirtualViewRmi extends Remote, VirtualView{

    /** {@inheritDoc} */
    @Override void showGameDelta(GameDelta delta) throws RemoteException;

    /** {@inheritDoc} */
    @Override void showInitialSnapshot(GameSnapshot snapshot) throws RemoteException;

    /** {@inheritDoc} */
    @Override void reportError(String errorMessage) throws RemoteException;

    /**
     * No-op method used as a liveness probe.
     *
     * <p>If the client is reachable, this call returns normally; if the client process
     * has crashed or is unreachable, RMI raises a {@link RemoteException}, which the
     * caller's heartbeat mechanism interprets as a client disconnection.
     *
     * @throws RemoteException if the client is unreachable
     */
    void ping() throws RemoteException;
}
