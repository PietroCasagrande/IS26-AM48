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
    private int ppToMin;    // salvato NEGATIVO
    private int ppToMax;    // salvato POSITIVO

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

        // numero massimo di stelle riscontrato
        int maxStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .max()
                .orElse(0);

        // numero minimo di stelle riscontrato
        int minStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);

        players.forEach(p -> {
            // tutti i player con le stelle massime ricevono punti (o doppi punti se si ha quell'edificio specifico)
            if (p.getShamanStars() == maxStars){
                p.updatePoints(ppToMax);
                if(p.deservesDoubleShamanPp()) p.updatePoints(ppToMax);
            }
            // tutti i player con le stelle minime perdono punti (a meno che non si abbia l'edificio che rende immuni)
            if(p.getShamanStars() == minStars){
                if(!p.isShamanSafe()) p.updatePoints(ppToMin);
            }
        });
    }
}
