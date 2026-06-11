package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that grants the current player the right to an additional pick.
 *
 * @see CardStrategy
 */
public class ExtraPickStrategy extends CardStrategy {
    /**
     * Creates a new extra-pick effect.
     *
     * @param registration the registration action attaching this effect to the pick channel
     */
    public ExtraPickStrategy(RegistrationAction registration) {
        super(registration);
    }

    /**
     * Grants the extra-pick right to the current player.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setExtraPickRight();
    }
}
