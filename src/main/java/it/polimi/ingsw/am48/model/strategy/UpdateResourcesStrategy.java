package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

public class UpdateResourcesStrategy extends CardStrategy{

    private final Resource resource;
    private final int quantity;

    public UpdateResourcesStrategy(Resource resource, int quantity, RegistrationAction registrationAction) {
        super(registrationAction);
        this.resource = resource;
        this.quantity = quantity;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        switch (resource) {
            case STAR -> playerContext.getCurrPlayer().updateShamanStars(quantity);    // sciamano (con 1-3 stelle) oppure edificio che assegna 3 stelle bonus
            case FOOD_DISCOUNT -> playerContext.getCurrPlayer().updateFoodDiscount(quantity);    // picker che dà 3
        }
    }
}
