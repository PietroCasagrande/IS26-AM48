package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.EventInfo;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.*;

/**
 * Client-side replica of the game state, built from a {@link GameSnapshot}
 * and updated incrementally via {@link GameDelta} objects received from the
 * server. The view ({@link it.polimi.ingsw.am48.view.gui.GUIViewFxml GUI} or
 * {@link it.polimi.ingsw.am48.view.tui.CLIView TUI}) reads this state to
 * render the game.
 *
 * <p>Fields mirror the server-side game: board rows, offer track, turn order,
 * player states, current phase or turn or era and end-game results.</p>
 */
public class ClientGameState {
    private String gameId;
    private int currentTurn;
    private String currentPhase;
    private int currEra;
    private List<String> upperRowCardIds;
    private List<String> lowerRowCardIds;
    private List<String> buildingUpperIds;
    private List<String> buildingLowerIds;
    private Map<String, ClientPlayerState> players;
    private Map<Character, String> offerTrackPositions;
    private List<String> offerTurnCardOrder;
    private String winnerNickname;
    private List<GameResult> leaderboard = new ArrayList<>();
    private List<EventInfo> lastEvents = new ArrayList<>();

    /**
     * Constructs a {@code ClientGameState} from a server-provided
     * {@link GameSnapshot}. This is called once when the client first
     * joins a game or reconnects.
     *
     * @param snapshot the full game snapshot received from the server
     * @return a fully populated {@code ClientGameState}
     */
    public static ClientGameState fromSnapshot(GameSnapshot snapshot) {
        ClientGameState state = new ClientGameState();
        state.gameId = snapshot.getGameId();
        state.currentTurn = snapshot.getCurrentTurn();
        state.currentPhase = snapshot.getPhase().getPhaseName();

        if(snapshot.getBoard() != null) {
            state.upperRowCardIds = new ArrayList<>(snapshot.getBoard().getUpperRowCardIds());
            state.lowerRowCardIds = new ArrayList<>(snapshot.getBoard().getLowerRowCardIds());
            state.buildingUpperIds = new ArrayList<>(snapshot.getBoard().getBuildingUpperRowCardIds());
            state.buildingLowerIds = new ArrayList<>(snapshot.getBoard().getBuildingLowerRowCardIds());
            state.offerTrackPositions = new HashMap<>(snapshot.getBoard().getOfferTrack().getTotemPositions());
            state.offerTurnCardOrder = new ArrayList<>(snapshot.getBoard().getOfferTurnCard().getTotemOrder());
            state.currEra = snapshot.getBoard().getCurrEra();
        } else {
            state.upperRowCardIds = new ArrayList<>();
            state.lowerRowCardIds = new ArrayList<>();
            state.buildingUpperIds = new ArrayList<>();
            state.buildingLowerIds = new ArrayList<>();
            state.offerTrackPositions = new HashMap<>();
            state.offerTurnCardOrder = new ArrayList<>();
            state.currEra = 0;
        }

        state.players = new HashMap<>();
        snapshot.getPlayerContext().getPlayers().forEach(p ->
                state.players.put(p.getNickname(), ClientPlayerState.fromSnapshot(p))
        );
        return state;
    }

    /**
     * Replaces the upper and lower tribe card rows shown on the board.
     *
     * @param upper the new list of card IDs for the upper tribe row
     * @param lower the new list of card IDs for the lower tribe row
     */
    public void updateTribeShowed(List<String> upper, List<String> lower) {
        this.upperRowCardIds = new ArrayList<>(upper);
        this.lowerRowCardIds = new ArrayList<>(lower);
    }

    /**
     * Replaces the upper and lower building card rows shown on the board.
     *
     * @param upper the new list of card IDs for the upper building row
     * @param lower the new list of card IDs for the lower building row
     */
    public void updateBuildingShowed(List<String> upper, List<String> lower) {
        this.buildingUpperIds = new ArrayList<>(upper);
        this.buildingLowerIds = new ArrayList<>(lower);
    }

    /**
     * Updates the food count for the specified player.
     *
     * @param nickname the player whose food to update
     * @param food the new food value
     */
    public void updatePlayerFood(String nickname, int food) {
        players.get(nickname).setFood(food);
    }

    /**
     * Updates the prestige points for the specified player.
     *
     * @param nickname the player whose points to update
     * @param points the new points value
     */
    public void updatePlayerPoints(String nickname, int points) {
        players.get(nickname).setPoints(points);
    }

    /**
     * Adds a character card ID to the specified player's collection.
     *
     * @param nickname the player receiving the card
     * @param cardId the character card identifier
     */
    public void addCharacterToPlayer(String nickname, String cardId) {
        players.get(nickname).addCharacterCard(cardId);
    }

    /**
     * Adds a building card ID to the specified player's collection.
     *
     * @param nickname the player receiving the card
     * @param cardId   the building card identifier
     */
    public void addBuildingToPlayer(String nickname, String cardId) {
        players.get(nickname).addBuildingCard(cardId);
    }

    /**
     * Places a player's totem on the offer track at the specified position.
     *
     * @param letter the offer track position (A-G)
     * @param nickname the player placing the totem
     */
    public void placeTotemOnTrack(char letter, String nickname) {
        offerTrackPositions.put(letter, nickname);
    }

    /**
     * Removes a player's nickname from the turn order card,
     * that would mean the totem has been placed on the track.
     *
     * @param nickname the player whose totem was placed
     */
    public void removeTotemFromTurnCard(String nickname) {
        offerTurnCardOrder.remove(nickname);
    }

    /**
     * Returns a player's totem to the turn order card (totem collected from the track).
     * Clears the totem from its offer track position and re-adds the player to the
     * turn order list.
     *
     * @param nickname the player whose totem was returned
     */
    public void returnTotemToTurnCard(String nickname) {
        offerTrackPositions.entrySet().stream()
                .filter(e -> e.getValue().equals(nickname))
                .findFirst()
                .ifPresent(e -> e.setValue(""));
        offerTurnCardOrder.add(nickname);
    }

    /**
     * Replaces the entire turn order list.
     *
     * @param newOrder the new turn order (nicknames in sequence)
     */
    public void updateOfferTurnCardOrder(List<String> newOrder) {
        this.offerTurnCardOrder = new ArrayList<>(newOrder);
    }


    /**
     * Sets the current phase name.
     *
     * @param phaseName the name of the new phase
     */
    public void setPhase(String phaseName) {
        this.currentPhase = phaseName;
    }

    /**
     * Sets the current turn number.
     *
     * @param newTurn the new turn value
     */
    public void incrementTurn(int newTurn) {
        this.currentTurn = newTurn;
    }

    /**
     * Sets the current era index.
     *
     * @param newEra the new era index (0 = FIRST, 1 = SECOND, 2 = THIRD)
     */
    public void setEra(int newEra) { this.currEra = newEra; }

    /**
     * Sets the nickname of the game winner.
     *
     * @param winnerNickname the winner's nickname
     */
    public void setWinnerNickname(String winnerNickname) {
        this.winnerNickname = winnerNickname;
    }

    /**
     * Sets the intergalactic leaderboard data.
     *
     * @param leaderboard the list of historical game results
     */
    public void setLeaderboard(List<GameResult> leaderboard) {this.leaderboard = leaderboard; }

    /**
     * Sets the event information for the last resolved turn.
     *
     * @param events the list of event info objects
     */
    public void setEvents(List<EventInfo> events) { this.lastEvents = new ArrayList<>(events); }

    /**
     * Returns the event information for the last resolved turn.
     *
     * @return an unmodifiable list of event info
     */
    public List<EventInfo> getEvents() { return lastEvents; }

    /**
     * Returns the game identifier.
     *
     * @return the game ID
     */
    public String getGameId() { return gameId; }

    /**
     * Returns the current turn number.
     *
     * @return the current turn
     */
    public int getCurrentTurn() { return currentTurn; }

    /**
     * Returns the name of the current phase.
     *
     * @return the phase name
     */
    public String getCurrentPhase() { return currentPhase; }

    /**
     * Returns the current era index.
     *
     * @return the era index (0 = FIRST, 1 = SECOND, 2 = THIRD)
     */
    public int getCurrEra() { return currEra; }

    /**
     * Returns the winner's nickname, or {@code null} if the game has not ended.
     *
     * @return the winner nickname, or {@code null}
     */
    public String getWinnerNickname() { return winnerNickname; }

    /**
     * Returns an unmodifiable view of the upper tribe card row IDs.
     *
     * @return the upper row card IDs
     */
    public List<String> getUpperRowCardIds() { return Collections.unmodifiableList(upperRowCardIds); }

    /**
     * Returns an unmodifiable view of the lower tribe card row IDs.
     *
     * @return the lower row card IDs
     */
    public List<String> getLowerRowCardIds() { return Collections.unmodifiableList(lowerRowCardIds); }

    /**
     * Returns an unmodifiable view of the upper building card row IDs.
     *
     * @return the upper building row card IDs
     */
    public List<String> getBuildingUpperIds() { return Collections.unmodifiableList(buildingUpperIds); }

    /**
     * Returns an unmodifiable view of the lower building card row IDs.
     *
     * @return the lower building row card IDs
     */
    public List<String> getBuildingLowerIds() { return Collections.unmodifiableList(buildingLowerIds); }

    /**
     * Returns an unmodifiable view of the offer track positions (letter → nickname).
     *
     * @return the offer track position map
     */
    public Map<Character, String> getOfferTrackPositions() { return Collections.unmodifiableMap(offerTrackPositions); }

    /**
     * Returns an unmodifiable view of the current turn order.
     *
     * @return the turn order list (nicknames in sequence)
     */
    public List<String> getOfferTurnCardOrder() { return Collections.unmodifiableList(offerTurnCardOrder); }

    /**
     * Returns the client-side state for a specific player by nickname.
     *
     * @param nickname the player's nickname
     * @return the {@link ClientPlayerState} for that player, or {@code null}
     */
    public ClientPlayerState getPlayer(String nickname) { return players.get(nickname); }

    /**
     * Returns an unmodifiable view of all players keyed by nickname.
     *
     * @return the players map
     */
    public Map<String, ClientPlayerState> getPlayers() { return Collections.unmodifiableMap(players); }

    /**
     * Returns an unmodifiable collection of all player states.
     *
     * @return all {@link ClientPlayerState} instances
     */
    public Collection<ClientPlayerState> getAllPlayers() { return Collections.unmodifiableCollection(players.values()); }

    /**
     * Returns an unmodifiable view of the leaderboard data.
     *
     * @return the leaderboard list
     */
    public List<GameResult> getLeaderboard() {return Collections.unmodifiableList(leaderboard); }
}