package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

public interface GamePhase {
    default void addPlayer(Game game, String playerNickname){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }
    default GameDelta placeTotem(Game game, Player player, char position){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }
    default GameDelta takeCard(Game game, Player player, String cardId){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }
    default GameDelta endTurn (Game game){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }

    PhaseSnapshot toSnapshot();
}
