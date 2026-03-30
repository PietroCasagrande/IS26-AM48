package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ResourcePerCharStrategy extends CardStrategy{
    private final Resource resource;
    private final int quantity;
    private final CharacterType characterType;

    public ResourcePerCharStrategy(Resource resource, int quantity, CharacterType characterType, RegistrationAction registration) {
        super(registration);
        this.resource = resource;
        this.quantity = quantity;
        this.characterType = characterType;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        int amount = quantity * playerContext.getCurrPlayer().getTribe().countByType(characterType);
        switch (resource) {
            case FOOD -> playerContext.getCurrPlayer().updateFood(amount);
            case FOOD_DISCOUNT ->  playerContext.getCurrPlayer().updateFoodDiscount(amount);
            case PRESTIGE_POINT -> playerContext.getCurrPlayer().updatePoints(amount);
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        }
    }
}
