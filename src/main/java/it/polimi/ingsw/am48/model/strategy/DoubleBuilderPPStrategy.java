package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that doubles the prestige points the player earns from his builders (at the end of the game).
 * <p>
 * The effect adds the current builder points on top of themselves, effectively doubling
 * the contribution of the builders accumulated so far.
 *
 * @see CardStrategy
 * @see BuilderStrategy
 */
public class DoubleBuilderPPStrategy extends CardStrategy {

    /**
     * Creates a new double-builder-points effect.
     *
     * @param registration the registration action attaching this effect to its trigger channel
     */
    public DoubleBuilderPPStrategy(RegistrationAction registration) {
        super(registration);
    }

    /**
     * Doubles the current player's builder points.
     *
     * @param playerContext the context of all the players and the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        Player player = playerContext.getCurrPlayer();
        player.updateBuilderPoints(player.getBuilderPoints());
    }
}
