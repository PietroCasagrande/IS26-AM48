package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferTurnCardDTO;
import it.polimi.ingsw.am48.model.board.OfferTurnCard;

import java.util.ArrayList;
import java.util.List;

public class OfferTurnFactory implements BoardFactory<OfferTurnCard> {
    private final List<OfferTurnCardDTO> offerTurn;

    public OfferTurnFactory(List<OfferTurnCardDTO> offerTurn) { this.offerTurn = offerTurn; }

    @Override
    public List<OfferTurnCard> createCards(int numPlayers){
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<OfferTurnCard> cards = new ArrayList<>();

        for(OfferTurnCardDTO dto : this.offerTurn){
            if(dto.numPlayers == numPlayers){
                cards.add(new OfferTurnCard(dto.numPlayers, dto.foodRewards, dto.ppPenalty));
            }
        }

        return cards;
    }
}
