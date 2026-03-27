package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;

public class EndTurnPhase implements GamePhase {
    @Override
    public GameDelta endTurn(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    public void onEventNotify(Game game){
        throw new UnsupportedOperationException("TODO");
    }
}
