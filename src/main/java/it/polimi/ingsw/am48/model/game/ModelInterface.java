package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

public interface ModelInterface {
    // metodi lobby: gestione partite
    void joinGame(int numPlayers, String nickname);

    // metodi in-game: azioni del giocatore
    GameDelta placeTotem(String nickname, char position);
    GameDelta takeCard(String name, String cardId);

    // metodo getSnapshot: inizializzazione e a seguito di un crash, per la persistenza
    GameSnapshot getSnapshot(String nickname);
}
