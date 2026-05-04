package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClientModel {
    private ClientGameState state;
    private final Set<ModelObserver> observers = new LinkedHashSet<>();  // valutare di cambiare in LinkedHashSet (mantiene ordine ma evita duplicati), così è impossibile registrare due volte lo stesso oggetto erroneamente

    public void registerObserver(ModelObserver observer) {
        observers.add(observer);
    }

    // --- inizializzazione ---

    public void setInitialState(GameSnapshot snapshot) {
        this.state = ClientGameState.fromSnapshot(snapshot);
        notifyObservers();
    }

    public void applyDelta(GameDelta delta) {
        if(state==null){
            notifyError("Received game update before initial snapshot");
            return;
        }

        delta.applyTo(this);
        notifyObservers();
    }

    public void notifyError(String message) {
         observers.forEach(o -> o.onError(message));
    }

    // --- metodi chiamati da applyTo() dei delta ---

    public void updateTribeShowed(List<String> upper, List<String> lower) {
        state.updateTribeShowed(upper, lower);
    }

    public void updateBuildingShowed(List<String> upper, List<String> lower) {
        state.updateBuildingShowed(upper, lower);
    }

    public void updatePlayerFood(String nickname, int food) {
        state.updatePlayerFood(nickname, food);
    }

    public void updatePlayerPoints(String nickname, int points) {
        state.updatePlayerPoints(nickname, points);
    }

    public void addCardToPlayerTribe(String nickname, String cardId) {
        state.addCharacterToPlayer(nickname, cardId);
    }

    public void addBuildingToPlayer(String nickname, String cardId) {
        state.addBuildingToPlayer(nickname, cardId);
    }

    public void placeTotemOnTrack(char letter, String nickname) {
        state.placeTotemOnTrack(letter, nickname);
        state.removeTotemFromTurnCard(nickname);
    }

    public void returnTotemToTurnCard(String nickname) {
        state.returnTotemToTurnCard(nickname);
    }

    public void updateOfferTurnCardOrder(List<String> newOrder) {
        state.updateOfferTurnCardOrder(newOrder);
    }

    public void setPhase(String phaseName) {
        state.setPhase(phaseName);
    }

    public void incrementTurn(int newTurn) {
        state.incrementTurn(newTurn);
    }

    // --- getter per la view ---

    public ClientGameState getState() { return state; }

    // --- notifica interna ---

    private void notifyObservers() {
        observers.forEach(o -> o.onStateUpdated(state));
    }
}