package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.model.delta.GameDelta;

import java.util.List;

public interface ModelInterface {
    // metodi lobby: gestione partite
    void joinGame(int numPlayers, String nickname);

    // metodi in-game: azioni del giocatore
    GameDelta placeTotem(String nickname, char position);
    List<GameDelta> takeCard(String name, String cardId);
}
