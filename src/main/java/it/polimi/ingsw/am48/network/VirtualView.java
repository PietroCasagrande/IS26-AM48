package it.polimi.ingsw.am48.network;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

public interface VirtualView {
    void showGameDelta(GameDelta delta) throws Exception;
    void showInitialSnapshot(GameSnapshot snapshot) throws Exception;
    void reportError(String errorMessage) throws Exception;
}
