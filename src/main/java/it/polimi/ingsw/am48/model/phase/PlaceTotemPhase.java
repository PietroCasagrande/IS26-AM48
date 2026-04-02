package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlaceTotemPhaseSnapshot;

import java.util.Set;
import java.util.stream.Collectors;

public class PlaceTotemPhase implements GamePhase {
    private Set<Player> playersPlaced;

    @Override
    public GameDelta placeTotem(Game game, Player player, char position){
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseSnapshot toSnapshot() {
        Set<String> placedNicknames = playersPlaced.stream()
                .map(Player::getNickname)
                .collect(Collectors.toSet());
        return new PlaceTotemPhaseSnapshot(placedNicknames);
    }
}
