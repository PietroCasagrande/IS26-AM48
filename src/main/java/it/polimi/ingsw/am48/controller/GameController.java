package it.polimi.ingsw.am48.controller;

import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.game.ModelInterface;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;

public class GameController {
    private final ModelInterface model;

    public GameController(ModelInterface model) {
        this.model = model;
    }

    // client si unisce alla partita
    public JoinResult handleJoinGame(int numPlayers, String nickname){
        return model.joinGame(numPlayers, nickname);
    }

    public boolean isGameFull(String nickname) {
        return model.isGameFull(nickname);
    }

    // client piazza il totem
    public List<GameDelta> handlePlaceTotem(String nickname, char position){
        List<GameDelta> deltas = model.placeTotem(nickname, position);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return deltas;
    }

    // client prende una carta
    public List<GameDelta> handleTakeCard(String nickname, String cardId){
        List<GameDelta> deltas = model.takeCard(nickname, cardId);
        // TODO: serializzazione Json(?), sincronizzazione(?)
        return deltas;
    }

    public List<String> getPlayersInGame(String nickname) {
        return model.getPlayersInGame(nickname);
    }
}
