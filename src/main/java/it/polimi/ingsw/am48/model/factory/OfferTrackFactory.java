package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;

import java.util.ArrayList;
import java.util.List;

public class OfferTrackFactory implements BoardFactory<OfferCard>{
    private final List<OfferCardDTO> track;

    public OfferTrackFactory(List<OfferCardDTO> track) {
        this.track = track;
    }

    @Override
    public List<OfferCard> createCards(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<OfferCard> offerTrack = new ArrayList<>();

        for (OfferCardDTO dto : this.track) {
            offerTrack.add(new OfferCard(dto.id, dto.numUp, dto.numDown, dto.foodBonus, dto.minPlayers));
        }

        return offerTrack;
    }
}
