package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.strategy.*;

import java.util.ArrayList;
import java.util.List;

public class CharacterFactory implements BoardFactory<CharacterCard> {
    private final List<CardDTO> characters;

    public CharacterFactory(List<CardDTO> characters) {
        this.characters = characters;
    }

    @Override
    public List<CharacterCard> createCards(int numPlayers) {
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<CharacterCard> characterDeck = new ArrayList<>();

        for(CardDTO dto : this.characters){
            if(dto.minPlayers <= numPlayers){
                Era era = Era.valueOf(dto.era);
                CharacterType type = CharacterType.valueOf(dto.character);

                CardStrategy strategy = null;
                if (dto.strategy != null) strategy = buildStrategy(dto.strategy);

                characterDeck.add(new CharacterCard(dto.id, era, strategy, type, dto.minPlayers));
            }
        }

        return characterDeck;
    }

    // Creates character's strategy: one-shot strategy, doesn't register to NotificatorCenter (empty lambda)
    private CardStrategy buildStrategy(StrategyDTO dto){
        switch (dto.effect)
            {
                case "BuilderStrategy": {
                    return new BuilderStrategy(dto.num2, dto.num1, (nc, pc, s) -> {});
                }
                case "InventorStrategy": {
                    Artifact artifact = Artifact.valueOf(dto.artifact);
                    return new InventorStrategy(artifact, (nc, pc, s) -> {});
                }
                case "ResourcePerCharStrategy": {
                    Resource resource = Resource.valueOf(dto.resource);
                    CharacterType type = CharacterType.valueOf(dto.character);
                    return new ResourcePerCharStrategy(resource, dto.num1, type, (nc, pc, s) -> {});
                }
                case "UpdateResourcesStrategy": {
                    Resource resource = Resource.valueOf(dto.resource);
                    return new UpdateResourcesStrategy(resource,  dto.num1, (nc, pc, s) -> {});
                }
                default: throw new IllegalArgumentException("Invalid strategy");
            }
    }
}
