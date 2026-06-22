package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that grants one extra food whenever the player gains food from returning to the offer turn card.
 * <p>
 * Triggered at pick time, the bonus is applied only if the player, after the pick, returns on one
 * of the offer squares that yield food ({@code deservesExtraFood}); in any other case the
 * effect does nothing.
 *
 * @see CardStrategy
 */
public class ExtraFoodOnFoodStrategy extends CardStrategy {

    /**
     * Creates a new extra-food-on-food effect.
     *
     * @param registration the registration action attaching this effect to the totem returned channel
     */
    public ExtraFoodOnFoodStrategy(RegistrationAction registration) {
        super(registration);
    }

    /**
     * Adds one extra food to the current player when they qualify for the food bonus.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        // we give +1 extra food only if the player is on one of the OfferTurnCard squares that gives food
        Player player = playerContext.getCurrPlayer();
        if(player.deservesExtraFood()) player.updateFood(1);
    }
}
