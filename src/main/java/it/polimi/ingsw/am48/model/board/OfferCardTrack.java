package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.*;

/**
 * Represents the offer track made by offer cards on the game board.
 * <p>
 * This class manages the available {@link OfferCard} slots and the placement of
 * players' totems during the place totem phase. It maintains the cards in a specific
 * order (typically alphabetical) to resolve the turn progression correctly.
 */
public class OfferCardTrack {
    private final Map<Character, OfferCard> track;

    /**
     * Creates a new {@code OfferCardTrack} using the provided map of offer cards.
     *
     * @param track a map linking each card's letter identifier to its {@link OfferCard} instance.
     * It must be a {@link TreeMap} (or similar sorted map) to guarantee the correct
     * evaluation order of the cards.
     */
    public OfferCardTrack(Map<Character, OfferCard> track){
        this.track = track;
    }

    /**
     * Places a player's totem on the specified offer card.
     *
     * @param player   the player placing the totem
     * @param letterId the character identifier of the target offer card
     * @throws IllegalArgumentException if the specified letter identifier does not correspond to a valid card on the track
     */
    public void placeTotem(Player player, char letterId){
        OfferCard offerCard = this.track.get(letterId);
        if(offerCard == null){ throw new IllegalArgumentException("Cannot place totem: invalid letter id"); }
        offerCard.placeTotem(player);
    }

    /**
     * Determines the order in which players will execute the offer phase, based on
     * the sequential order of the cards where their totems are placed.
     *
     * @return an ordered list of players who have placed a totem on the track
     */
    public List<Player> getPickOrder(){
        return this.track.values()
                .stream()
                .map(OfferCard::getTotem)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Finds the specific offer card currently occupied by the given player's totem.
     *
     * @param player the player whose totem position is being searched
     * @return the {@link OfferCard} containing the player's totem
     * @throws IllegalArgumentException if the player has no totem placed on any card in the track
     */
    public OfferCard findTrackPosition(Player player){
        return this.track.values()
                .stream()
                .filter(o -> o.getTotem().equals(Optional.of(player)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find track position for player " + player));
    }

    /**
     * Creates a snapshot of the current state of the offer track for network synchronization.
     *
     * @return an {@link OfferTrackSnapshot} containing the mapping between card identifiers
     * and the nicknames of the players occupying them
     */
    public OfferTrackSnapshot toSnapshot() {
        Map<Character, String> totemPositions = new HashMap<>();
        for (Map.Entry<Character, OfferCard> entry : track.entrySet()) {
            String nickname = entry.getValue().getTotem()
                    .map(Player::getNickname)
                    .orElse("");
            totemPositions.put(entry.getKey(), nickname);
        }
        return new OfferTrackSnapshot(totemPositions);
    }

    /**
     * Recreates the {@code OfferCardTrack} state from a network snapshot and the base game data.
     * This method is primarily used for server persistency and restoring a game state.
     *
     * @param snapshot   the serialized state containing the saved totem positions
     * @param numPlayers the number of players in the game, used to filter out invalid offer cards
     * @param players    the list of active {@link Player} instances to map the saved nicknames to actual objects
     * @return the restored {@code OfferCardTrack} matching the snapshot state
     */
    public static OfferCardTrack fromSnapshot(OfferTrackSnapshot snapshot, int numPlayers, List<Player> players) {
        BoardDTO dto = new GameDataLoader().loadData();
        Map<Character, OfferCard> trackMap = new TreeMap<>();

        for (OfferCardDTO offerDto : dto.offerCards) {
            if (offerDto.minPlayers <= numPlayers) {
                trackMap.put(offerDto.id, new OfferCard(
                        offerDto.id, offerDto.numUp, offerDto.numDown,
                        offerDto.foodBonus, offerDto.minPlayers
                ));
            }
        }

        for (Map.Entry<Character, String> entry : snapshot.getTotemPositions().entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            char space = entry.getKey();
            String nickname = entry.getValue();
            Player player = players.stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst().orElseThrow();
            trackMap.get(space).placeTotem(player);
        }

        return new OfferCardTrack(trackMap);
    }
}
