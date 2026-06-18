package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that doubles the prestige points the player gains from shaman events.
 * <p>
 * Rather than awarding points directly, it sets a flag on the player so that, when a
 * {@link ShamanEventStrategy} resolves, the points granted to the player holding the most
 * stars are doubled.
 *
 * @see CardStrategy
 * @see ShamanEventStrategy
 */
public class DoubleShamanPPStrategy extends CardStrategy {
    /**
     * Creates a new double-shaman-points effect.
     *
     * @param registration the registration action attaching this effect to its trigger channel
     */
    public DoubleShamanPPStrategy(RegistrationAction registration) {
        super(registration);
    }

    /**
     * Flags the current player so that future shaman-event points are doubled.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setShamanDoubling();
    }
}
