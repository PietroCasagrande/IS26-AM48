package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.board.OfferCardTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A concrete factory responsible for instantiating the {@link OfferCardTrack}.
 * <p>
 * This factory processes a list of data transfer objects (DTOs) representing individual
 * offer track slots. It filters out the slots that are not valid for the current number
 * of players and aggregates the valid ones into a structured and correctly ordered track.
 */
public class OfferTrackFactory implements BoardFactory<OfferCardTrack>{
    private final List<OfferCardDTO> track;

    /**
     * Creates a new {@code OfferTrackFactory} initialized with the base track data.
     *
     * @param track a list of {@link OfferCardDTO} containing the blueprints for all possible offer track slots
     */
    public OfferTrackFactory(List<OfferCardDTO> track) {
        this.track = track;
    }

    /**
     * Generates a list containing the single {@link OfferCardTrack} configured for the current game.
     * <p>
     * The factory iterates through the available blueprints, selecting only the slots where
     * the minimum player requirement is satisfied.
     * These slots are placed into a {@link TreeMap} to strictly guarantee their alphabetical
     * evaluation order before being wrapped into the track object.
     *
     * @param numPlayers the number of players in the game (must be between 2 and 5)
     * @return a list containing the fully initialized {@link OfferCardTrack}
     * @throws IllegalArgumentException if the number of players is outside the valid range (2-5)
     */
    @Override
    public List<OfferCardTrack> createCards(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<OfferCardTrack> offerTrack = new ArrayList<>();
        Map<Character, OfferCard> map = new TreeMap<>();

        for (OfferCardDTO dto : this.track) {
            if(dto.minPlayers <= numPlayers){
                map.put(dto.id, new OfferCard(dto.id, dto.numUp, dto.numDown, dto.foodBonus, dto.minPlayers));
            }
        }
        offerTrack.add(new OfferCardTrack(map));

        return offerTrack;
    }
}
