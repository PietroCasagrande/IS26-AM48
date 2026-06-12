package it.polimi.ingsw.am48.network;

/**
 * Technology-agnostic interface representing the server, as seen from the client side.
 *
 * <p>This interface decouples the client's command-sending logic from the underlying
 * transport: both the Socket and RMI implementations expose the same three game actions,
 * so the rest of the client code (e.g. {@code view.tui.CLIView}) can issue commands without
 * knowing whether they travel over a socket stream or an RMI remote call.
 *
 * <p>Concrete implementations:
 * <ul>
 *   <li>RMI: {@link it.polimi.ingsw.am48.network.client.RmiClient}</li>
 *   <li>Socket: {@link it.polimi.ingsw.am48.network.client.SocketServerHandler}</li>
 * </ul>
 *
 * @see VirtualServerRmi
 * @see VirtualServerSocket
 * @see VirtualView
 */
public interface VirtualServer {

    /**
     * Sends a request to join or create a game with the given number of players.
     *
     * @param numPlayers the desired game size
     * @param nickname   the player's chosen display name
     * @throws Exception if the request cannot be sent (transport failure) or is rejected
     *                    by the server (game-rule violation)
     */
    void joinGame(int numPlayers, String nickname) throws Exception;

    /**
     * Sends a request to place the player's totem on the given offer-track slot.
     *
     * @param nickname the nickname of the player performing the action
     * @param position the offer-track slot (A–G) on which the totem should be placed
     * @throws Exception if the request cannot be sent or is rejected by the server
     */
    void placeTotem(String nickname, char position) throws Exception;

    /**
     * Sends a request to take a card from the showed rows.
     *
     * @param nickname the nickname of the player performing the action
     * @param cardId   the identifier of the card to take
     * @throws Exception if the request cannot be sent or is rejected by the server
     */
    void takeCard(String nickname, String cardId) throws Exception;

    /**
     * Notifies the server that the client is disconnecting cleanly.
     *
     * <p>Default no-op implementation: not all transports require an explicit disconnect
     * notification (e.g. closing a socket stream is itself the signal), so implementations
     * override this only when a transport-specific cleanup call is needed.
     *
     * @throws Exception if the disconnect notification cannot be sent
     */
    default void disconnect() throws Exception {}
}
