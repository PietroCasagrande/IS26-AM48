package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.List;

public class OfferTrackFactory implements BoardFactory<OfferCard>{
    private final List<OfferCardDTO> track;

    public OfferTrackFactory(List<OfferCardDTO> track) {
        this.track = track;
    }

    @Override
    public List<OfferCard> createCards(int numPlayers) {
        List<OfferCard> offerTrack = new ArrayList<>();

        for(OfferCardDTO dto : this.track){
            char id = dto.id;

            CardStrategy strategy = null;
            if(dto.strategy != null){
                strategy = buildStrategy(dto.strategy);
            }

            offerTrack.add(new OfferCard(id, strategy, dto.minPlayers));
        }

        return offerTrack;
    }

    private CardStrategy buildStrategy(StrategyDTO strategy) {throw new UnsupportedOperationException("TO DO");}
}
