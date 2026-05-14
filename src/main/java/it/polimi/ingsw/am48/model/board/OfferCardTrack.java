package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.*;

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
    public List<Player> getPickOrder(){
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
        Map<Character, String> totemPositions = new HashMap<>();
        for(Map.Entry<Character, OfferCard> entry : track.entrySet()){
            entry.getValue().getTotem().ifPresent(player ->
                    totemPositions.put(
                            entry.getKey(), player.getNickname()
                    )
            );
        }

        return new OfferTrackSnapshot(totemPositions);
    }

    public static OfferCardTrack fromSnapshot(OfferTrackSnapshot snapshot, int numPlayers, List<Player> players) {
        // Ricostruisce le OfferCard dal JSON (struttura fissa, non cambia)
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

        // Applica i totem presenti al momento del salvataggio
        for (Map.Entry<Character, String> entry : snapshot.getTotemPositions().entrySet()) {
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
