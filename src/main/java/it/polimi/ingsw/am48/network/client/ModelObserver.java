package it.polimi.ingsw.am48.network.client;

/**
 * Observer interface for receiving game state updates and error
 * notifications from the {@link ClientModel}. Implemented by the
 * view layer ({@link it.polimi.ingsw.am48.view.gui.GUIViewFxml GUI}
 * or {@link it.polimi.ingsw.am48.view.tui.CLIView TUI}) to react
 * to state changes and display errors to the user.
 */
public interface ModelObserver {
    /**
     * Called whenever the game state is updated (after an initial
     * snapshot or a delta has been applied).
     *
     * @param state the updated {@link ClientGameState}
     */
    void onStateUpdated(ClientGameState state);

    /**
     * Called when an error occurs (e.g. invalid action, network failure).
     *
     * @param message a description of the error
     */
    void onError(String message);
}
