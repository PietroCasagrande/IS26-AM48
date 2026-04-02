package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.snapshot.EndTurnPhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

public class EndTurnPhase implements GamePhase {
    @Override
    public GameDelta endTurn(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    public void onEventNotify(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndTurnPhaseSnapshot();
    }
}
