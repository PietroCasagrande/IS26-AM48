package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Effect that grants the current player a fixed quantity of a given resource.
 * <p>
 * Shared by characters and buildings, this strategy applies a flat amount according to
 * the resource kind:
 * <ul>
 *     <li>{@code STAR} &ndash; adds shaman stars (e.g. the Shaman character or a bonus-star building);</li>
 *     <li>{@code FOOD_DISCOUNT} &ndash; increases the food discount;</li>
 *     <li>{@code FOOD} &ndash; adds an immediate amount of food;</li>
 *     <li>{@code PRESTIGE_POINT} &ndash; adds {@code quantity} points for every completed
 *     character set the player owns (e.g. the building awarding points per full set).</li>
 * </ul>
 *
 * @see CardStrategy
 */
public class UpdateResourcesStrategy extends CardStrategy{

    private final Resource resource;
    private final int quantity;

    /**
     * Creates a new fixed-resource effect.
     *
     * @param resource           the kind of resource to grant
     * @param quantity           the amount applied (its exact meaning depends on the resource)
     * @param registrationAction the registration action describing when the effect triggers
     */
    public UpdateResourcesStrategy(Resource resource, int quantity, RegistrationAction registrationAction) {
        super(registrationAction);
        this.resource = resource;
        this.quantity = quantity;
    }

    /**
     * Applies the configured resource update to the current player.
     *
     * @param playerContext the context of all players and current player
     * @throws IllegalArgumentException if the configured resource is not supported
     */
    @Override
    public void effect(PlayerContext playerContext) {
        Player player = playerContext.getCurrPlayer();
        switch (resource) {
            case STAR -> player.updateShamanStars(quantity);    // shaman (with 1-3 stars) or building that grants 3 bonus stars
            case FOOD_DISCOUNT -> player.updateFoodDiscount(quantity);    // picker that gives 3 (discount meant as positive)
            case FOOD -> player.updateFood(quantity);    // offerCard A (+3 instant food)
            case PRESTIGE_POINT -> player.updatePoints(quantity * player.getTribe().minListSize());    // building that gives 5 PP for each complete set (building 11)
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        }
    }
}
