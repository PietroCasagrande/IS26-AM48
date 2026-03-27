// BoardSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.util.List;
import java.util.Map;

public class BoardSnapshot {
    private final List<String> upperRowCardIds;
    private final List<String> lowerRowCardIds;
    private final List<String> buildingUpperRowCardIds;
    private final List<String> buildingLowerRowCardIds;
    private final List<String> tribeDeckRemainingIds;
    private final Map<String, List<String>> buildingDeckRemainingIds; // Era -> List<cardId>
    private final OfferTrackSnapshot offerTrack;
    private final OfferTurnCardSnapshot offerTurnCard;

    public BoardSnapshot(List<String> upperRowCardIds, List<String> lowerRowCardIds,
                         List<String> buildingUpperRowCardIds, List<String> buildingLowerRowCardIds,
                         List<String> tribeDeckRemainingIds,
                         Map<String, List<String>> buildingDeckRemainingIds,
                         OfferTrackSnapshot offerTrack, OfferTurnCardSnapshot offerTurnCard) {
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
    public Map<String, List<String>> getBuildingDeckRemainingIds() { return buildingDeckRemainingIds; }
    public OfferTrackSnapshot getOfferTrack() { return offerTrack; }
    public OfferTurnCardSnapshot getOfferTurnCard() { return offerTurnCard; }
}
