package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import it.polimi.ingsw.am48.model.strategy.RegistrationAction;

import java.util.ArrayList;
import java.util.List;

public class BuildingFactory implements BoardFactory<BuildingCard> {
    private final List<CardDTO> buildings;

    public BuildingFactory(List<CardDTO> buildings) {
        this.buildings = buildings;
    }

    @Override
    public List<BuildingCard> createCards(int numPlayers) {
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<BuildingCard> buildingDeck = new ArrayList<>();

        for (CardDTO dto : this.buildings) {
            Era era =  Era.valueOf(dto.era);

            CardStrategy strategy = null;
            if(dto.strategy != null){
                strategy = buildStrategy(dto.strategy);
            }

            buildingDeck.add(new BuildingCard(dto.id, era, strategy, dto.foodCost, dto.prestigePoints));
        }

        return  buildingDeck;
    }

    private CardStrategy buildStrategy(StrategyDTO strategy) {throw new UnsupportedOperationException("TO DO");}

    private RegistrationAction buildRegistrationAction(CardDTO dto) {throw new UnsupportedOperationException("TO DO");}
}
