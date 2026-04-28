package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

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
        Player player = playerContext.getCurrPlayer();
        switch (resource) {
            case STAR -> player.updateShamanStars(quantity);    // sciamano (con 1-3 stelle) oppure edificio che assegna 3 stelle bonus
            case FOOD_DISCOUNT -> player.updateFoodDiscount(quantity);    // picker che dà 3 (sconto inteso positivo)
            case FOOD -> player.updateFood(quantity);    // offerCard A (+3 cibo istantanei)
            case PRESTIGE_POINT -> player.updatePoints(quantity * player.getTribe().minListSize());    // edificio che dà 5 PP per ogni set completo (edificio 11)
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        }
    }
}
