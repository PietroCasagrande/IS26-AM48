package it.polimi.ingsw.am48.network;

/**
 * Socket-specific marker interface for {@link VirtualView}.
 *
 * <p>Unlike {@link VirtualViewRmi}, the Socket transport requires no additional methods
 * or {@code throws} refinements: notifications are simply serialised to JSON and written to
 * the socket's output stream. This interface exists purely to mirror the {@code *Rmi} /
 * {@code *Socket} naming symmetry and to give the Socket implementation
 * ({@link it.polimi.ingsw.am48.network.server.SocketClientHandler}) an explicit,
 * transport-specific type.
 *
 * @see VirtualView
 * @see VirtualViewRmi
 * @see it.polimi.ingsw.am48.network.server.SocketClientHandler
 */
public interface VirtualViewSocket extends VirtualView {}
