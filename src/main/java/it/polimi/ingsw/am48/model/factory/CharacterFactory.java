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

/**
 * A concrete factory responsible for instantiating {@link CharacterCard} objects.
 * <p>
 * This factory processes a list of data transfer objects (DTOs) representing the base
 * card templates and generates the actual deck of characters. It filters the available
 * cards based on the number of players and dynamically resolves and binds the
 * appropriate {@link CardStrategy} to each character.
 */
public class CharacterFactory implements BoardFactory<CharacterCard> {
    private final List<CardDTO> characters;

    /**
     * Creates a new {@code CharacterFactory} initialized with the base character data.
     *
     * @param characters a list of {@link CardDTO} containing the static attributes and
     *                   strategy blueprints for all available characters
     */
    public CharacterFactory(List<CardDTO> characters) {
        this.characters = characters;
    }

    /**
     * Generates the deck of character cards tailored for the specified number of players.
     * <p>
     * Cards with a minimum player requirement strictly greater than the provided
     * {@code numPlayers} are excluded from the final deck.
     *
     * @param numPlayers the number of players in the game (must be between 2 and 5)
     * @return a fully initialized list of {@link CharacterCard} valid for the current game
     * @throws IllegalArgumentException if {@code numPlayers} is less than 2 or greater than 5
     */
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

    /**
     * Constructs the specific strategy effect for a character based on its blueprint.
     * <p>
     * Note: Character strategies are inherently "one-shot" or immediate effects.
     * Therefore, they are instantiated with an empty lambda function for their
     * registration phase, as they do not need to actively register and listen
     * to the observer pattern handled by the {@code NotificatorCenter}.
     *
     * @param dto the strategy data transfer object containing the effect type and its parameters
     * @return the instantiated {@link CardStrategy}
     * @throws IllegalArgumentException if the effect type specified in the DTO is unknown or invalid
     */
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
