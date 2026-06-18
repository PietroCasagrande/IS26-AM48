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

/**
 * Represents the phase where players place their totems on the offer track.
 * <p>
 * During this state, players select an available slot on the offer track to determine
 * their playing order for the subsequent phase, consisting in picking up cards.
 * The phase tracks which players have already acted and automatically transitions
 * the game to the {@link PlayerOfferPhase} once everyone has placed their totem.
 */
public class PlaceTotemPhase implements GamePhase {

    private final Set<Player> playersPlaced;

    /**
     * Constructs a fresh {@code PlaceTotemPhase} at the beginning of the cycle.
     */
    public PlaceTotemPhase() {
        this.playersPlaced = new HashSet<Player>();
    }

    /**
     * Reconstructs the phase from a saved state.
     * <p>
     * This constructor is utilized exclusively by {@link GamePhase#fromSnapshot}
     * during the server recovery process to restore the exact state of players
     * who had already placed their totems before the crash.
     *
     * @param playersPlaced the set of players who have already completed their turn in this phase
     */
    public PlaceTotemPhase(Set<Player> playersPlaced) {
        this.playersPlaced = new HashSet<>(playersPlaced);
    }

    /**
     * Handles the placement of a player's totem on the board.
     * <p>
     * The method validates that the player hasn't already acted. If valid, the board
     * registers the placement. If this action concludes the phase (i.e., all players
     * have placed their totems), the state automatically shifts to the {@code PlayerOfferPhase}.
     *
     * @param game     the main game instance
     * @param player   the player placing the totem
     * @param position the character identifier of the chosen track slot
     * @return a list of {@link GameDelta} to be broadcasted to clients to update their UI
     * @throws InvalidActionException if the player attempts to place a totem more than once
     */
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
            deltas.add(new TotemPlacedDelta(player.getNickname(), position, updatedTurnOrderNicknames, "PLAYER_OFFER"));
            playerOfferPhase.setup(game).ifPresent(deltas::add);
        }
        else deltas.add(new TotemPlacedDelta(player.getNickname(), position, updatedTurnOrderNicknames, "PLACE_TOTEM"));

        return deltas;
    }

    /**
     * Serializes this phase into a snapshot.
     *
     * @return a {@link PlaceTotemPhaseSnapshot} containing the nicknames of the players who have already acted
     */
    @Override
    public PhaseSnapshot toSnapshot() {
        Set<String> placedNicknames = playersPlaced.stream()
                .map(Player::getNickname)
                .collect(Collectors.toSet());
        return new PlaceTotemPhaseSnapshot(placedNicknames);
    }

    /**
     * Retrieves the set of players who have already placed their totems.
     * <p>
     * <i>Note: This method is exposed strictly for unit testing purposes.</i>
     *
     * @return the set of players that have acted
     */
    public Set<Player> getPlayersPlaced() {
        return playersPlaced;
    }
}
