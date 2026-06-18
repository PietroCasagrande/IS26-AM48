package it.polimi.ingsw.am48.network;

/**
 * Socket-specific marker interface for {@link VirtualServer}.
 *
 * <p>Unlike {@link VirtualServerRmi}, the Socket transport requires no additional methods
 * or {@code throws} refinements: commands are simply serialised to JSON and written to the
 * socket's output stream. This interface exists purely to mirror the {@code *Rmi} /
 * {@code *Socket} naming symmetry and to give the Socket implementation
 * ({@link it.polimi.ingsw.am48.network.client.SocketServerHandler}) an explicit,
 * transport-specific type.
 *
 * @see VirtualServer
 * @see VirtualServerRmi
 * @see it.polimi.ingsw.am48.network.client.SocketServerHandler
 */
public interface VirtualServerSocket extends VirtualServer {}