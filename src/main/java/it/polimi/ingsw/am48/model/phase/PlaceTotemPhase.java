package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlaceTotemPhaseSnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaceTotemPhase implements GamePhase {
    private Set<Player> playersPlaced;
    private List<Player> placeOrder;

    public PlaceTotemPhase(List<Player> placeOrder) {
        this.placeOrder = placeOrder;
        this.playersPlaced = new HashSet<Player>();
    }

    @Override
    public GameDelta placeTotem(Game game, Player player, char position){
        // Checks player's turn
        if(this.playersPlaced.contains(player)){
            throw new InvalidActionException("Cannot place totem: your totem is already in place.");
        }

        // Places totem
        game.getBoard().placeTotem(player, position);
        this.playersPlaced.add(player);

        // Checks whether all totems have been placed
        if(this.playersPlaced.size() == game.getNumPlayers()){
            List<Player> trackOrder = game.getBoard().getPickOrder();
            game.setPhase(new PlayerOfferPhase(trackOrder));
        }

        // Returns a new game delta
        return new TotemPlacedDelta(player.getNickname(), position);
    }

    @Override
    public PhaseSnapshot toSnapshot() {
        Set<String> placedNicknames = playersPlaced.stream()
                .map(Player::getNickname)
                .collect(Collectors.toSet());
        return new PlaceTotemPhaseSnapshot(placedNicknames);
    }
}
