package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlaceTotemPhaseSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaceTotemPhase implements GamePhase {
    private Set<Player> playersPlaced;

    public PlaceTotemPhase() {
        this.playersPlaced = new HashSet<Player>();
    }

    @Override
    public List<GameDelta> placeTotem(Game game, Player player, char position){
        List<GameDelta> deltas = new ArrayList<>();

        // Checks player's turn
        if(this.playersPlaced.contains(player)){
            throw new InvalidActionException("Cannot place totem: your totem is already in place.");
        }

        // Places totem
        game.getBoard().placeTotem(player, position);
        this.playersPlaced.add(player);

        List<String> updatedTurnOrderNicknames = new ArrayList<>(game.getBoard().getPlaceOrder().stream().map(Player::getNickname).toList());

        // Checks whether all totems have been placed
        if(this.playersPlaced.size() == game.getNumPlayers()){
            List<Player> trackOrder = game.getBoard().getPickOrder();
            PlayerOfferPhase playerOfferPhase = new PlayerOfferPhase(trackOrder);
            game.setPhase(playerOfferPhase);
            deltas.add(new TotemPlacedDelta(player.getNickname(), position, updatedTurnOrderNicknames, game.getCurrentPhase().toSnapshot().getPhaseName()));
            playerOfferPhase.setup(game).ifPresent(deltas::add);  // se setup restituisce un delta lo aggiungiamo alla lista deltas
        }
        else deltas.add(new TotemPlacedDelta(player.getNickname(), position, updatedTurnOrderNicknames, game.getCurrentPhase().toSnapshot().getPhaseName()));

        return deltas;
    }

    @Override
    public PhaseSnapshot toSnapshot() {
        Set<String> placedNicknames = playersPlaced.stream()
                .map(Player::getNickname)
                .collect(Collectors.toSet());
        return new PlaceTotemPhaseSnapshot(placedNicknames);
    }
}
