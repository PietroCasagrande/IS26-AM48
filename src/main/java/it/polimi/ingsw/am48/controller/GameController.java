package it.polimi.ingsw.am48.controller;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.game.ModelInterface;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;

public class GameController {
    private final ModelInterface model;



    public GameController(GameManager gameManager) {
        this.model = gameManager;
    }

    // client si unisce alla partita
    public GameSnapshot handleJoinGame(int numPlayers, String nickname){
        model.joinGame(numPlayers, nickname);
        return model.getSnapshotForNickname(nickname);
        }

    public boolean isGameFull(String nickname) {
        return model.isGameFull(nickname);
    }

    // client piazza il totem
    public GameDelta handlePlaceTotem(String nickname, char position){
        GameDelta delta = model.placeTotem(nickname, position);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return delta;
    }

    // client prende una carta
    public List<GameDelta> handleTakeCard(String nickname, String cardId){
        List<GameDelta> deltas = model.takeCard(nickname, cardId);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return deltas;
    }
}
