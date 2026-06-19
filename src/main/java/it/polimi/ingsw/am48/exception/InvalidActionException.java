package it.polimi.ingsw.am48.exception;

/**
 * Thrown when a player attempts an action that violates the game rules or the
 * current game state (e.g. placing a totem on an occupied slot, taking a card
 * out of turn, or joining with a nickname already in use).
 *
 * <p>Extends {@link RuntimeException} so it can propagate through the controller
 * and network layers without being declared on every intermediate method signature.
 * It is caught at the boundary of each command handler (Socket and RMI alike) and
 * converted into an {@link it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification}
 * sent back to the offending client.
 */
public class InvalidActionException extends RuntimeException {

    /**
     * Constructs an {@code InvalidActionException} with the given detail message,
     * which is forwarded to the client as the human-readable error description.
     *
     * @param message a description of the violated rule or invalid state
     */
    public InvalidActionException(String message) {
        super(message);
    }
}