package it.polimi.ingsw.am48.model.enums;

/**
 * Identifies the kind of reward produced by a card effect.
 * <p>
 * This enum is not a stored currency: it is a <em>discriminator</em> used to parametrize the
 * generic resource-granting strategies shared by both Character and Building cards
 * ({@link it.polimi.ingsw.am48.model.strategy.UpdateResourcesStrategy} for flat amounts and
 * {@link it.polimi.ingsw.am48.model.strategy.ResourcePerCharStrategy} for amounts scaled by the
 * number of characters of a given type). The same effect class can therefore serve many different
 * cards: the {@code Resource} value selects which player counter the effect updates and how the
 * configured quantity is interpreted, avoiding a dedicated strategy class per reward type.
 * <p>
 * The values map to the game concepts described in the rulebook (food tokens, Prestige Points,
 * famine discounts and Shaman stars).
 *
 * @see it.polimi.ingsw.am48.model.strategy.UpdateResourcesStrategy
 * @see it.polimi.ingsw.am48.model.strategy.ResourcePerCharStrategy
 */
public enum Resource {
    /**
     * Food tokens granted immediately to the player. Food is the resource spent to feed the tribe
     * during the Sustenance (famine) event or to buy building cards.
     */
    FOOD,

    /**
     * Combined food-and-points reward applied in a single effect: it grants one food per matching
     * character <em>plus</em> Prestige Points scaled by the same count. It exists as a distinct tag
     * so cards that award both at once (e.g. the building giving 1 food and prestige per Hunter, and
     * the Hunt event resolution) can be expressed with one strategy instead of two separate effects.
     */
    FOOD_POINTS,

    /**
     * Increases the player's food discount, i.e. the amount of discount they have when paying food to
     * feed the tribe during the famine/Sustenance event (for example the picker's 3-food discount,
     * or buildings granting a discount per owned character of a given type).
     */
    FOOD_DISCOUNT,

    /**
     * Prestige Points (PP), the victory points that determine the winner at the end of the game.
     */
    PRESTIGE_POINT,

    /**
     * Shaman stars: the Shaman icons compared between players during the Shamanic Ritual event,
     * where holding more stars than the opponents awards the event's Prestige Points.
     */
    STAR
}
