// BoardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class BoardSnapshot implements Serializable {
    private final List<String> upperRowCardIds;
    private final List<String> lowerRowCardIds;
    private final List<String> buildingUpperRowCardIds;
    private final List<String> buildingLowerRowCardIds;
    private final List<String> tribeDeckRemainingIds;
    private final List<String> buildingDeckRemainingIds; // Era -> List<cardId>
    private final OfferTrackSnapshot offerTrack;
    private final OfferTurnCardSnapshot offerTurnCard;

    @JsonCreator
    public BoardSnapshot(
            @JsonProperty("upperRowCardIds") List<String> upperRowCardIds,
            @JsonProperty("lowerRowCardIds") List<String> lowerRowCardIds,
            @JsonProperty("buildingUpperRowCardIds") List<String> buildingUpperRowCardIds,
            @JsonProperty("buildingLowerRowCardIds") List<String> buildingLowerRowCardIds,
            @JsonProperty("tribeDeckRemainingIds") List<String> tribeDeckRemainingIds,
            @JsonProperty("buildingDeckRemainingIds") List<String> buildingDeckRemainingIds,
            @JsonProperty("offerTrack") OfferTrackSnapshot offerTrack,
            @JsonProperty("offerTurnCard") OfferTurnCardSnapshot offerTurnCard) {
        this.upperRowCardIds = upperRowCardIds;
        this.lowerRowCardIds = lowerRowCardIds;
        this.buildingUpperRowCardIds = buildingUpperRowCardIds;
        this.buildingLowerRowCardIds = buildingLowerRowCardIds;
        this.tribeDeckRemainingIds = tribeDeckRemainingIds;
        this.buildingDeckRemainingIds = buildingDeckRemainingIds;
        this.offerTrack = offerTrack;
        this.offerTurnCard = offerTurnCard;
    }

    public List<String> getUpperRowCardIds() { return upperRowCardIds; }
    public List<String> getLowerRowCardIds() { return lowerRowCardIds; }
    public List<String> getBuildingUpperRowCardIds() { return buildingUpperRowCardIds; }
    public List<String> getBuildingLowerRowCardIds() { return buildingLowerRowCardIds; }
    public List<String> getTribeDeckRemainingIds() { return tribeDeckRemainingIds; }
    public List<String> getBuildingDeckRemainingIds() { return buildingDeckRemainingIds; }
    public OfferTrackSnapshot getOfferTrack() { return offerTrack; }
    public OfferTurnCardSnapshot getOfferTurnCard() { return offerTurnCard; }
}
