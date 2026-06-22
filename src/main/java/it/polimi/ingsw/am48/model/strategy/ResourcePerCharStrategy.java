package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Effect that grants a resource scaled by the number of characters of a given type the
 * current player owns.
 * <p>
 * This strategy is shared by both characters and buildings: the amount awarded is
 * {@code quantity * (number of characters of characterType in the tribe)}. The kind of
 * resource determines how the amount is applied:
 * <ul>
 *     <li>{@code FOOD} &ndash; adds food (e.g. the Hunter character);</li>
 *     <li>{@code FOOD_POINTS} &ndash; adds one food per character plus the scaled prestige
 *     points (e.g. the building granting food and points per hunter);</li>
 *     <li>{@code FOOD_DISCOUNT} &ndash; increases the food discount for famine;</li>
 *     <li>{@code PRESTIGE_POINT} &ndash; adds prestige points.</li>
 * </ul>
 *
 * @see CardStrategy
 */
public class ResourcePerCharStrategy extends CardStrategy{
    private final Resource resource;
    private final int quantity;
    private final CharacterType characterType;

    /**
     * Creates a new per-character resource effect.
     *
     * @param resource      the kind of resource to grant
     * @param quantity      the amount granted for each owned character of {@code characterType}
     * @param characterType the character type whose count drives the reward
     * @param registration  the registration action describing when the effect triggers
     */
    public ResourcePerCharStrategy(Resource resource, int quantity, CharacterType characterType, RegistrationAction registration) {
        // FOR NOW MARKED AS PERSISTENT, BUT TO BE REVIEWED FOR THE HUNTER CHARACTER
        super(registration);
        this.resource = resource;
        this.quantity = quantity;
        this.characterType = characterType;
    }

    /**
     * Grants the configured resource to the current player, scaled by how many characters
     * of {@code characterType} are in their tribe.
     *
     * @param playerContext the context of the current player and all the players in game
     * @throws IllegalArgumentException if the configured resource is not supported
     */
    @Override
    public void effect(PlayerContext playerContext) {
        int num = playerContext.getCurrPlayer().getTribe().countByType(characterType);
        int amount = quantity * num;
        switch (resource) {
            case FOOD -> playerContext.getCurrPlayer().updateFood(amount);    // hunter character
            case FOOD_POINTS -> {    // building 7 (1 food and 1 pp per hunter) and perhaps called by HuntEvent
                playerContext.getCurrPlayer().updateFood(num);    // food updated is always +1
                playerContext.getCurrPlayer().updatePoints(amount);    // pp updated are 1 for building 7 but 1-3 for the hunt event (in case this strategy is to be called iteratively inside HuntEvent)
            }
            case FOOD_DISCOUNT ->  playerContext.getCurrPlayer().updateFoodDiscount(amount);    // famine discount buildings
            case PRESTIGE_POINT -> playerContext.getCurrPlayer().updatePoints(amount);    // pp buildings at the end of the game
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        }
    }
}
