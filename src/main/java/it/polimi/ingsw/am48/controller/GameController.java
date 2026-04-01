package it.polimi.ingsw.am48.controller;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.ModelInterface;

public class GameController {
    private final ModelInterface model;

    public GameController(ModelInterface model) {
        this.model = model;
    }

    // client si unisce alla partita
    void handleJoinGame(int numPlayers, String nickname){
        model.joinGame(numPlayers, nickname);
        // TODO: check se il game è full(?), eventualmente inviare SnapShot(?)
    }

    // client piazza il totem
    GameDelta handlePlaceTotem(String nickname, char position){
        GameDelta delta = model.placeTotem(nickname, position);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return delta;
    }

    // client prende una carta
    GameDelta handleTakeCard(String nickname, String cardId){
        GameDelta delta = model.takeCard(nickname, cardId);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return delta;
    }
}
