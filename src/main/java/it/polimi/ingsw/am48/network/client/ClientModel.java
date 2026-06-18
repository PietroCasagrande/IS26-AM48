package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.EventInfo;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-side model that holds the local replica of the game state
 * ({@link ClientGameState}) and notifies registered {@link ModelObserver
 * observers} whenever the state changes. It receives an initial
 * {@link GameSnapshot} on join/reconnect and incremental {@link GameDelta}
 * updates throughout the game.
 *
 * <p>This is the connection between the network layer (which receives
 * deltas) and the view layer, which renders the state.
 * </p>
 */
public class ClientModel {
    private ClientGameState state;
    // Observers are stored in a LinkedHashSet to preserve insertion
    // order while preventing duplicate registrations
    private final Set<ModelObserver> observers = new LinkedHashSet<>();

    private String sessionNickname;
    private int sessionNumPlayers;

    // Guards against processing further game updates after the game has ended
    private boolean gameEnded = false;

    /**
     * Saves the session parameters for use during reconnection or display.
     *
     * @param nickname the player's nickname
     * @param numPlayers the number of players in the game
     */
    public void saveSession(String nickname, int numPlayers) {
        this.sessionNickname = nickname;
        this.sessionNumPlayers = numPlayers;
    }

    /**
     * Registers an observer to receive state update notifications.
     * Duplicate registrations are silently ignored.
     *
     * @param observer the observer to register
     */
    public void registerObserver(ModelObserver observer) {
        observers.add(observer);
    }

    /**
     * Unregisters a previously registered observer.
     *
     * @param observer the observer to remove
     */
    public void unregisterObserver(ModelObserver observer) {observers.remove(observer);}

    /**
     * Replaces the full game state with the data from a {@link GameSnapshot}.
     * Called when the client first joins or reconnects to a game.
     *
     * @param snapshot the initial game snapshot from the server
     */
    public void setInitialState(GameSnapshot snapshot) {
        this.state = ClientGameState.fromSnapshot(snapshot);
        this.gameEnded = false;
        notifyObservers();
    }

    /**
     * Applies a single {@link GameDelta} to the current game state.
     * Delegates to {@link GameDelta#applyTo(ClientModel)} and notifies all
     * observers. If the state has not been initialized yet, an error is
     * reported instead.
     *
     * @param delta the delta to apply
     */
    public void applyDelta(GameDelta delta) {
        if(state==null){
            notifyError("Received game update before initial snapshot");
            return;
        }

        delta.applyTo(this);
        notifyObservers();
    }

    /**
     * Notifies all observers of an error message.
     *
     * @param message the error description
     */
    public void notifyError(String message) {
         observers.forEach(o -> o.onError(message));
    }

    /**
     * Updates the shown tribe card rows.
     *
     * @param upper the new upper tribe row card IDs
     * @param lower the new lower tribe row card IDs
     */
    public void updateTribeShowed(List<String> upper, List<String> lower) {
        state.updateTribeShowed(upper, lower);
    }

    /**
     * Updates the shown building card rows.
     *
     * @param upper the new upper building row card IDs
     * @param lower the new lower building row card IDs
     */
    public void updateBuildingShowed(List<String> upper, List<String> lower) {
        state.updateBuildingShowed(upper, lower);
    }

    /**
     * Updates a player's food count.
     *
     * @param nickname the player's nickname
     * @param food the new food value
     */
    public void updatePlayerFood(String nickname, int food) {
        state.updatePlayerFood(nickname, food);
    }

    /**
     * Updates a player's prestige points.
     *
     * @param nickname the player's nickname
     * @param points the new points value
     */
    public void updatePlayerPoints(String nickname, int points) {
        state.updatePlayerPoints(nickname, points);
    }

    /**
     * Adds a character card to a player's tribe.
     *
     * @param nickname the player's nickname
     * @param cardId the character card identifier
     */
    public void addCardToPlayerTribe(String nickname, String cardId) {
        state.addCharacterToPlayer(nickname, cardId);
    }

    /**
     * Adds a building card to a player's collection.
     *
     * @param nickname the player's nickname
     * @param cardId the building card identifier
     */
    public void addBuildingToPlayer(String nickname, String cardId) {
        state.addBuildingToPlayer(nickname, cardId);
    }

    /**
     * Places a player's totem on the offer track and removes them from
     * the turn order list.
     *
     * @param letter the offer track position
     * @param nickname the player placing the totem
     */
    public void placeTotemOnTrack(char letter, String nickname) {
        state.placeTotemOnTrack(letter, nickname);
        state.removeTotemFromTurnCard(nickname);
    }

    /**
     * Returns a player's totem to the turn order card.
     *
     * @param nickname the player whose totem is returned
     */
    public void returnTotemToTurnCard(String nickname) {
        state.returnTotemToTurnCard(nickname);
    }

    /**
     * Replaces the turn order list.
     *
     * @param newOrder the new turn order (nicknames in sequence)
     */
    public void updateOfferTurnCardOrder(List<String> newOrder) {
        state.updateOfferTurnCardOrder(newOrder);
    }

    /**
     * Sets the current phase name.
     *
     * @param phaseName the new phase name
     */
    public void setPhase(String phaseName) {
        state.setPhase(phaseName);
    }

    /**
     * Sets the current turn number.
     *
     * @param newTurn the new turn value
     */
    public void incrementTurn(int newTurn) {
        state.incrementTurn(newTurn);
    }

    /**
     * Sets the current era index.
     *
     * @param newEra the new era index (0 = FIRST, 1 = SECOND, 2 = THIRD)
     */
    public void setEra(int newEra) { state.setEra(newEra); }

    /**
     * Sets the winner's nickname.
     *
     * @param winnerNickname the winner's nickname
     */
    public void setWinnerNickname(String winnerNickname) { state.setWinnerNickname(winnerNickname); }

    /**
     * Sets the leaderboard data.
     *
     * @param leaderboard the list of historical game results
     */
    public void setLeaderboard(List<GameResult> leaderboard) { state.setLeaderboard(leaderboard);}

    /**
     * Sets the event information for the last resolved turn.
     *
     * @param events the list of event info objects
     */
    public void setEvents(List<EventInfo> events) { state.setEvents(events); }

    /**
     * Returns the current client-side game state.
     *
     * @return the {@link ClientGameState}, or {@code null} if not yet initialized
     */
    public ClientGameState getState() { return state; }

    /**
     * Notifies all registered observers that the state has changed.
     */
    private void notifyObservers() {
        observers.forEach(o -> o.onStateUpdated(state));
    }

    /**
     * Returns the nickname used in the current session.
     *
     * @return the session nickname
     */
    public String getSessionNickname() {
        return sessionNickname;
    }

    /**
     * Returns the number of players configured for the current session.
     *
     * @return the number of players
     */
    public int getSessionNumPlayers() {
        return sessionNumPlayers;
    }

    /**
     * Marks the game as ended, preventing further delta processing.
     */
    public void setGameEnded() { this.gameEnded = true; }

    /**
     * Returns whether the game has ended.
     *
     * @return {@code true} if the game has ended
     */
    public boolean isGameEnded() { return gameEnded; }
}