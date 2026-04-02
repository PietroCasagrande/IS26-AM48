package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

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

    @Override
    public PhaseSnapshot toSnapshot() {
        return new WaitingPhaseSnapshot();
    }

}
