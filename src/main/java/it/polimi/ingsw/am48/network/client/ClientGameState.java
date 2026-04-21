package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientGameState {
    private String gameId;
    private int currentTurn;
    private String currentPhase;

    // board
    private List<String> upperRowCardIds;
    private List<String> lowerRowCardIds;
    private List<String> buildingUpperIds;
    private List<String> buildingLowerIds;

    // players
    private Map<String, ClientPlayerState> players;

    // offer track
    private Map<Character, String> offerTrackPositions; // lettera -> nickname

    // costruito dal primo GameSnapshot
    public static ClientGameState fromSnapshot(GameSnapshot snapshot) {
        ClientGameState state = new ClientGameState();
        state.gameId = snapshot.getGameId();
        state.currentTurn = snapshot.getCurrentTurn();
        state.currentPhase = snapshot.getPhase().getPhaseName();
        state.upperRowCardIds = new ArrayList<>(snapshot.getBoard().getUpperRowCardIds());
        state.lowerRowCardIds = new ArrayList<>(snapshot.getBoard().getLowerRowCardIds());
        state.buildingUpperIds = new ArrayList<>(snapshot.getBoard().getBuildingUpperRowCardIds());
        state.buildingLowerIds = new ArrayList<>(snapshot.getBoard().getBuildingLowerRowCardIds());
        state.players = new HashMap<>();
        snapshot.getPlayers().forEach(p ->
                state.players.put(p.getNickname(), ClientPlayerState.fromSnapshot(p))
        );
        return state;
    }

    // metodi di aggiornamento chiamati da applyTo() dei delta
    public void updateTribeShowed(List<String> upper, List<String> lower) {
        this.upperRowCardIds = new ArrayList<>(upper);
        this.lowerRowCardIds = new ArrayList<>(lower);
    }

    public void updateBuildingShowed(List<String> upper, List<String> lower) {
        this.buildingUpperIds = new ArrayList<>(upper);
        this.buildingLowerIds = new ArrayList<>(lower);
    }

    public void updatePlayerFood(String nickname, int food) {
        players.get(nickname).setFood(food);
    }

    public void updatePlayerPoints(String nickname, int points) {
        players.get(nickname).setPoints(points);
    }

    public void addCardToPlayerTribe(String nickname, String cardId) {
        players.get(nickname).addCard(cardId);
    }

    public void incrementTurn() { this.currentTurn++; }
    public void setPhase(String phase) { this.currentPhase = phase; }

    // getters per la view
    public List<String> getUpperRowCardIds() { return upperRowCardIds; }
    // ecc.
}