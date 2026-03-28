package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;

public class WaitingForPlayersPhase implements GamePhase {
    private int requiredPlayer;

    public WaitingForPlayersPhase(int requiredPlayer) {
        this.requiredPlayer = requiredPlayer;
    }

    @Override
    public void addPlayer(Game game, String playerNickname){
        throw new UnsupportedOperationException("TODO");
    }

    /*
        @Override
        public void setupGame(Game g){
            throw new UnsupportedOperationException("TODO");
        }
    */

}
