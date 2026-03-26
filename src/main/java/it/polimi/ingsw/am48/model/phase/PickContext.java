package it.polimi.ingsw.am48.model.phase;

public interface PickContext {
    int getPicksUp();
    int getPicksDown();
    void increasePicksUp();
    void increasePicksDown();
    void nextPlayer();
}
