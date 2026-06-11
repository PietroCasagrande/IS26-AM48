package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that protects the player from shaman-event penalties.
 * <p>
 * It sets a safety flag on the player so that, when a {@link ShamanEventStrategy} resolves,
 * the player holding the fewest stars does not lose any prestige points.
 *
 * @see CardStrategy
 * @see ShamanEventStrategy
 */
public class ShamanSafetyStrategy extends CardStrategy {

    /**
     * Creates a new shaman-safety effect.
     *
     * @param registration the registration action attaching this effect to its trigger channel
     */
    public ShamanSafetyStrategy(RegistrationAction registration) {
        super(registration);
    }

    /**
     * Flags the current player as immune to shaman-event point losses.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setShamanSafety();
    }
}
