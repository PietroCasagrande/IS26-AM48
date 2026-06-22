package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Sustenance (famine) event effect, applied to every player when the event resolves.
 * <p>
 * Each player must feed their tribe: the food due equals their total number of characters
 * reduced by their food discount. If a positive amount remains, the player pays it in food
 * and, for any food they cannot cover, loses {@code ppLostPerChar} prestige points per
 * missing unit.
 *
 * @see CardStrategy
 */
public class SustenanceStrategy extends CardStrategy {
    private int ppLostPerChar;    // stored with a POSITIVE sign

    /**
     * Creates a new sustenance event effect.
     *
     * @param ppLostPerChar the prestige points lost for each unit of food the player cannot pay (a positive value)
     * @param registration  the registration action attaching this effect to the picker event channel
     */
    public SustenanceStrategy(int ppLostPerChar, RegistrationAction registration) {
        super(registration);
        this.ppLostPerChar = ppLostPerChar;
    }

    /**
     * Charges every player the food needed to sustain their tribe, converting any shortfall
     * into a prestige point loss.
     *
     * @param playerContext the context granting access to all players
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getPlayers().forEach(p -> {
            int foodToPay = p.getTotalCharacters() - p.getFoodDiscount();
            if (foodToPay > 0) p.payFood(foodToPay, ppLostPerChar);
        });
    }
}
