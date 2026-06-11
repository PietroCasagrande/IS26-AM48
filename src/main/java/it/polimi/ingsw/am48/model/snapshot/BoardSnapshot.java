// BoardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.enums.Era;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable, immutable snapshot of the board state. It captures the cards
 * currently showed in the tribe and building rows, the remaining cards in both
 * decks, the {@link OfferTrackSnapshot offer track} and the
 * {@link OfferTurnCardSnapshot turn-order card}, plus era-related information.
 *
 * <p>Card collections are stored as lists of card ids rather than full card
 * objects; the ids are resolved back to {@code Card} instances when the game is
 * restored from the snapshot.
 * </p>
 *
 * @see GameSnapshot
 * @see OfferTrackSnapshot
 * @see OfferTurnCardSnapshot
 */
public class BoardSnapshot implements Serializable {
    private final List<String> upperRowCardIds;
    private final List<String> lowerRowCardIds;
    private final List<String> buildingUpperRowCardIds;
    private final List<String> buildingLowerRowCardIds;
    private final List<String> tribeDeckRemainingIds;
    private final List<String> buildingDeckRemainingIds; // Era -> List<cardId>
    private final OfferTrackSnapshot offerTrack;
    private final OfferTurnCardSnapshot offerTurnCard;
    private final List <Integer> buildingsPerEra;
    private final int currEra;
    private final int numPlayers;


    /**
     * Creates a new board snapshot.
     *
     * @param upperRowCardIds ids of the cards in the upper showed tribe row
     * @param lowerRowCardIds ids of the cards in the lower showed tribe row
     * @param buildingUpperRowCardIds ids of the cards in the upper showed
     *                                building row
     * @param buildingLowerRowCardIds ids of the cards in the lower showed
     *                                building row
     * @param tribeDeckRemainingIds ids of the cards still in the tribe deck
     * @param buildingDeckRemainingIds ids of the cards still in the building
     *                                 deck
     * @param offerTrack snapshot of the offer track (totem positions)
     * @param offerTurnCard snapshot of the turn-order card (totem order)
     * @param buildingsPerEra number of buildings to draw for each era
     * @param currEra the index of the current era
     * @param numPlayers the number of players in the game
     */
    @JsonCreator
    public BoardSnapshot(
            @JsonProperty("upperRowCardIds") List<String> upperRowCardIds,
            @JsonProperty("lowerRowCardIds") List<String> lowerRowCardIds,
            @JsonProperty("buildingUpperRowCardIds") List<String> buildingUpperRowCardIds,
            @JsonProperty("buildingLowerRowCardIds") List<String> buildingLowerRowCardIds,
            @JsonProperty("tribeDeckRemainingIds") List<String> tribeDeckRemainingIds,
            @JsonProperty("buildingDeckRemainingIds") List<String> buildingDeckRemainingIds,
            @JsonProperty("offerTrack") OfferTrackSnapshot offerTrack,
            @JsonProperty("offerTurnCard") OfferTurnCardSnapshot offerTurnCard,
            @JsonProperty("buildingsPerEra") List<Integer> buildingsPerEra,
            @JsonProperty("currEra") int currEra,
            @JsonProperty("numPlayers") int numPlayers) {
        this.upperRowCardIds = upperRowCardIds;
        this.lowerRowCardIds = lowerRowCardIds;
        this.buildingUpperRowCardIds = buildingUpperRowCardIds;
        this.buildingLowerRowCardIds = buildingLowerRowCardIds;
        this.tribeDeckRemainingIds = tribeDeckRemainingIds;
        this.buildingDeckRemainingIds = buildingDeckRemainingIds;
        this.offerTrack = offerTrack;
        this.offerTurnCard = offerTurnCard;
        this.buildingsPerEra = buildingsPerEra;
        this.currEra = currEra;
        this.numPlayers = numPlayers;
    }

    public List<String> getUpperRowCardIds() { return upperRowCardIds; }
    public List<String> getLowerRowCardIds() { return lowerRowCardIds; }
    public List<String> getBuildingUpperRowCardIds() { return buildingUpperRowCardIds; }
    public List<String> getBuildingLowerRowCardIds() { return buildingLowerRowCardIds; }
    public List<String> getTribeDeckRemainingIds() { return tribeDeckRemainingIds; }
    public List<String> getBuildingDeckRemainingIds() { return buildingDeckRemainingIds; }
    public OfferTrackSnapshot getOfferTrack() { return offerTrack; }
    public OfferTurnCardSnapshot getOfferTurnCard() { return offerTurnCard; }
    public List<Integer> getBuildingsPerEra() { return buildingsPerEra; }
    public int getCurrEra() { return currEra; }
    public int getNumPlayers() { return numPlayers; }
}
