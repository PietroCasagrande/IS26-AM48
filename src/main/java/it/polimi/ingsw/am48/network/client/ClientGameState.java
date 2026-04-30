package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.*;

public class ClientGameState {
    private String gameId;
    private int currentTurn;
    private String currentPhase;
    private List<String> upperRowCardIds;
    private List<String> lowerRowCardIds;
    private List<String> buildingUpperIds;
    private List<String> buildingLowerIds;
    private Map<String, ClientPlayerState> players;
    private Map<Character, String> offerTrackPositions; // lettera -> nickname
    private List<String> offerTurnCardOrder;            // ordine turno attuale
    private String winnerNickname;

    // --- costruzione da snapshot ---

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
        } else {
            state.upperRowCardIds = new ArrayList<>();
            state.lowerRowCardIds = new ArrayList<>();
            state.buildingUpperIds = new ArrayList<>();
            state.buildingLowerIds = new ArrayList<>();
            state.offerTrackPositions = new HashMap<>();
            state.offerTurnCardOrder = new ArrayList<>();
        }

        state.players = new HashMap<>();
        snapshot.getPlayers().forEach(p ->
                state.players.put(p.getNickname(), ClientPlayerState.fromSnapshot(p))
        );
        return state;
    }

    // --- aggiornamento showed ---

    public void updateTribeShowed(List<String> upper, List<String> lower) {
        this.upperRowCardIds = new ArrayList<>(upper);
        this.lowerRowCardIds = new ArrayList<>(lower);
    }

    public void updateBuildingShowed(List<String> upper, List<String> lower) {
        this.buildingUpperIds = new ArrayList<>(upper);
        this.buildingLowerIds = new ArrayList<>(lower);
    }

    // --- aggiornamento player ---

    public void updatePlayerFood(String nickname, int food) {
        players.get(nickname).setFood(food);
    }

    public void updatePlayerPoints(String nickname, int points) {
        players.get(nickname).setPoints(points);
    }

    public void addCharacterToPlayer(String nickname, String cardId) {
        players.get(nickname).addCharacterCard(cardId);
    }

    public void addBuildingToPlayer(String nickname, String cardId) {
        players.get(nickname).addBuildingCard(cardId);
    }

    // --- aggiornamento offer track ---

    public void placeTotemOnTrack(char letter, String nickname) {
        offerTrackPositions.put(letter, nickname);
    }

    // --- aggiornamento offer turn card ---

    public void removeTotemFromTurnCard(String nickname) {
        offerTurnCardOrder.remove(nickname);
    }

    public void returnTotemToTurnCard(String nickname) {
        offerTrackPositions.entrySet()
                .removeIf(e -> e.getValue().equals(nickname));
        offerTurnCardOrder.add(nickname);
    }

    public void updateOfferTurnCardOrder(List<String> newOrder) {
        this.offerTurnCardOrder = new ArrayList<>(newOrder);
    }

    // --- aggiornamento fase e turno ---

    public void setPhase(String phaseName) {
        this.currentPhase = phaseName;
    }

    public void incrementTurn(int newTurn) {
        this.currentTurn = newTurn;
    }

    // --- getters per la view ---

    public String getGameId() { return gameId; }
    public int getCurrentTurn() { return currentTurn; }
    public String getCurrentPhase() { return currentPhase; }
    public List<String> getUpperRowCardIds() { return Collections.unmodifiableList(upperRowCardIds); }
    public List<String> getLowerRowCardIds() { return Collections.unmodifiableList(lowerRowCardIds); }
    public List<String> getBuildingUpperIds() { return Collections.unmodifiableList(buildingUpperIds); }
    public List<String> getBuildingLowerIds() { return Collections.unmodifiableList(buildingLowerIds); }
    public Map<Character, String> getOfferTrackPositions() { return Collections.unmodifiableMap(offerTrackPositions); }
    public List<String> getOfferTurnCardOrder() { return Collections.unmodifiableList(offerTurnCardOrder); }
    public ClientPlayerState getPlayer(String nickname) { return players.get(nickname); }
    public Map<String, ClientPlayerState> getPlayers() { return Collections.unmodifiableMap(players); }
    public Collection<ClientPlayerState> getAllPlayers() { return Collections.unmodifiableCollection(players.values()); }
}