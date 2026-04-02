package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OfferCardTrack {
    private final Map<Character, OfferCard> track;  // Must be a TreeMap to guarantee OfferCard order

    public OfferCardTrack(Map<Character, OfferCard> track){
        this.track = track;
    }

    // Finds the requested offer card on which placing player totem
    public void placeTotem(Player player, char letterId){
        OfferCard offerCard = this.track.get(letterId);
        if(offerCard == null){ throw new IllegalArgumentException("Cannot place totem: invalid letter id"); }
        offerCard.placeTotem(player);
    }

    // Returns offer phase order based on players' placements
    public List<Player> getActionOrder(){
        return this.track.values()
                .stream()
                .map(OfferCard::getTotem)
                .flatMap(Optional::stream)
                .toList();
    }

    // Finds the offer track on which the requested player totem lies
    public OfferCard findTrackPosition(Player player){
        return this.track.values()
                .stream()
                .filter(o -> o.getTotem().equals(Optional.of(player)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find track position for player " + player));
    }

    public OfferTrackSnapshot toSnapshot() {
        Map<String, String> totemPositions = new HashMap<>();
        for(Map.Entry<Character, OfferCard> entry : track.entrySet()){
            entry.getValue().getTotem().ifPresent(player ->
                    totemPositions.put(
                            String.valueOf(entry.getKey()), player.getTotem().name()
                    )
            );
        }

        return new OfferTrackSnapshot(totemPositions);
    }
}
