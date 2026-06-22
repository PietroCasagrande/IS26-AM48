package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

/**
 * Shaman event effect, applied to every player when the event resolves.
 * <p>
 * The players are ranked by their number of shaman stars: everyone holding the maximum
 * gains {@code ppToMax} points (doubled if they own the relevant building, see
 * {@link DoubleShamanPPStrategy}), while everyone holding the minimum loses {@code ppToMin}
 * points (stored as a negative value), unless they are protected by a
 * {@link ShamanSafetyStrategy}.
 *
 * @see CardStrategy
 * @see DoubleShamanPPStrategy
 * @see ShamanSafetyStrategy
 */
public class ShamanEventStrategy extends CardStrategy {
    private int ppToMin;    // stored NEGATIVE
    private int ppToMax;    // stored POSITIVE

    /**
     * Creates a new shaman event effect.
     *
     * @param ppToMin      the prestige points lost by the players with the fewest stars (a negative value)
     * @param ppToMax      the prestige points gained by the players with the most stars (a positive value)
     * @param registration the registration action attaching this effect to the shaman event channel
     */
    public ShamanEventStrategy(int ppToMin, int ppToMax, RegistrationAction registration) {
        super(registration);
        this.ppToMin = ppToMin;
        this.ppToMax = ppToMax;
    }

    /**
     * Rewards the players with the most stars and penalizes those with the fewest, applying
     * the doubling and safety modifiers where present.
     *
     * @param playerContext the context granting access to all players
     */
    @Override
    public void effect(PlayerContext playerContext) {
        List<Player> players = playerContext.getPlayers();

        // maximum number of stars found
        int maxStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .max()
                .orElse(0);

        // minimum number of stars found
        int minStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);

        players.forEach(p -> {
            // all players with the maximum stars receive points (or double points if they own that specific building)
            if (p.getShamanStars() == maxStars){
                p.updatePoints(ppToMax);
                if(p.deservesDoubleShamanPp()) p.updatePoints(ppToMax);
            }
            // all players with the minimum stars lose points (unless they own the building that grants immunity)
            if(p.getShamanStars() == minStars){
                if(!p.isShamanSafe()) p.updatePoints(ppToMin);
            }
        });
    }
}
