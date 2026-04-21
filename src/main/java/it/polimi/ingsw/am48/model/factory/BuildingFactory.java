package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.strategy.*;

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

    private CardStrategy buildStrategy(StrategyDTO dto) {
        switch (dto.effect){
            case "ExtraFoodOnFoodStrategy": return new ExtraFoodOnFoodStrategy(buildRegistrationAction(dto));
            case "InventorsPairStrategy": return new InventorsPairStrategy(buildRegistrationAction(dto));
            case "ShamanSafetyStrategy": return new ShamanSafetyStrategy(buildRegistrationAction(dto));
            case "AllSetFoodStrategy": return new AllSetFoodStrategy(buildRegistrationAction(dto));
            case "HuntEventStrategy": return new HuntEventStrategy(dto.num1, buildRegistrationAction(dto));
            case "DoubleBuilderPPStrategy": return new DoubleBuilderPPStrategy(buildRegistrationAction(dto));
            case "DoubleShamanPPStrategy": return new DoubleShamanPPStrategy(buildRegistrationAction(dto));
            case "ExtraPickStrategy": return new ExtraPickStrategy(buildRegistrationAction(dto));
            case "ResourcePerCharStrategy": {
                Resource resource = Resource.valueOf(dto.resource);
                CharacterType type = CharacterType.valueOf(dto.character);
                return new ResourcePerCharStrategy(resource, dto.num1, type, buildRegistrationAction(dto));
            }
            case "UpdateResourcesStrategy":  {
                Resource resource = Resource.valueOf(dto.resource);
                return new UpdateResourcesStrategy(resource, dto.num1, buildRegistrationAction(dto));
            }
            default: throw new IllegalArgumentException("Invalid building strategy");
        }
    }

    private RegistrationAction buildRegistrationAction(StrategyDTO dto) {
        switch (dto.notificator) {
            case "OnTotemReturned": return (nc, pc, s) -> {nc.getTotemReturnedNotificator().attach(pc.getCurrPlayer(), s);};
            case "OnPick": return (nc, pc, s) -> {nc.getPickNotificator().attach(pc.getCurrPlayer(), s);};
            case "OnEvent": return (nc, pc, s) -> {nc.getEventNotificator().attach(EventType.valueOf(dto.eventType), pc.getCurrPlayer(), s);};
            case "OnEndOfferPhase": return (nc, pc, s) -> {nc.getEndOfferPhaseNotificator().attach(pc.getCurrPlayer(), s);};
            case "OnEndGame": return (nc, pc, s) -> {nc.getEndGameNotificator().attach(pc.getCurrPlayer(), s);};
            default: throw new IllegalArgumentException("Invalid building notificator");
        }
    }
}
