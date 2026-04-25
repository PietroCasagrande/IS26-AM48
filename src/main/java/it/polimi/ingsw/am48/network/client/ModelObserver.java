package it.polimi.ingsw.am48.network.client;

public interface ModelObserver {
    void onStateUpdated(ClientGameState state);
    void onError(String message);
}
