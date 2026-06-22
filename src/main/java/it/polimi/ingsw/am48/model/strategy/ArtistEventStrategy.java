package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Artist event effect, applied to every player when the event resolves.
 * <p>
 * Each player who owns at least {@code threshold} artists gains {@code ppPerArtist} prestige
 * points for every artist they hold; players below the threshold instead lose a fixed amount
 * of points ({@code ppLost}, stored as a negative value).
 *
 * @see CardStrategy
 */
public class ArtistEventStrategy extends CardStrategy {
    int threshold;
    int ppPerArtist;
    int ppLost;   // stored with a NEGATIVE sign

    /**
     * Creates a new artist event effect.
     *
     * @param threshold    the minimum number of artists required to be rewarded instead of penalized
     * @param ppPerArtist  the prestige points granted per artist when at or above the threshold
     * @param ppLost       the prestige points lost when below the threshold (a negative value)
     * @param registration the registration action attaching this effect to the artist event channel
     */
    public ArtistEventStrategy(int threshold, int ppPerArtist, int ppLost, RegistrationAction registration) {
        super(registration);
        this.threshold = threshold;
        this.ppPerArtist = ppPerArtist;
        this.ppLost = ppLost;
    }

    /**
     * Rewards or penalizes every player according to how many artists they own.
     *
     * @param playerContext the context granting access to all players
     */
    @Override
    public void effect(PlayerContext playerContext) {
        // grants so many pp for each artist owned if above the threshold, otherwise removes the indicated number of pp
        playerContext.getPlayers().forEach(p -> {
                int artistsCount = p.getTribe().countByType(CharacterType.ARTIST);
                if(artistsCount >= this.threshold) p.updatePoints(ppPerArtist * artistsCount);
                else p.updatePoints(ppLost);
            });
    }
}
