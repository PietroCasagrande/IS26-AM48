package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.Set;

public class PlaceTotemPhase implements GamePhase {
    private Set<Player> playersPlaced;

    @Override
    public GameDelta placeTotem(Game game, Player player, char position){
        throw new UnsupportedOperationException("TODO");
    }


}
