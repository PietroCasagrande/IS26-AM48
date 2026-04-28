package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.board.OfferCardTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OfferTrackFactory implements BoardFactory<OfferCardTrack>{
    private final List<OfferCardDTO> track;

    public OfferTrackFactory(List<OfferCardDTO> track) {
        this.track = track;
    }

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
