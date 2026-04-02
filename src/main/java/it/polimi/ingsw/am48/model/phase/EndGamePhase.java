package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.snapshot.EndGamePhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

public class EndGamePhase implements GamePhase {
    public void onEndGameNotify(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    public void computeEndGameScore(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndGamePhaseSnapshot();
    }
}
