package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Immediate effect of the Builder character.
 * <p>
 * When acquired, a builder grants the current player a permanent building discount
 * (applied to the food cost of future buildings) and contributes prestige points that
 * are awarded at the end of the game.
 *
 * @see CardStrategy
 */
public class BuilderStrategy extends CardStrategy {
    private final int builderPp;
    private final int buildingDiscount;

    /**
     * Creates a new builder effect.
     *
     * @param builderPp           the prestige points contributed by this builder at the end of the game
     * @param buildingDiscount    the food discount (a positive value) granted on future building purchases
     * @param registrationAction  the registration action; a no-op for this immediate effect
     */
    public BuilderStrategy(int builderPp, int buildingDiscount, RegistrationAction registrationAction) {
        super(registrationAction);
        this.builderPp = builderPp;
        this.buildingDiscount = buildingDiscount;
    }

    /**
     * Adds this builder's prestige points and building discount to the current player.
     *
     * @param playerContext the context of the player who acquired the builder
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().updateBuilderPoints(builderPp);    // punti assegnati dai builder a fine partita
        playerContext.getCurrPlayer().updateBuildingDiscount(buildingDiscount);    // sconto (positivo) dato dai builder
    }
}
