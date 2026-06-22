package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Hunt event effect, applied to every player when the event resolves.
 * <p>
 * Each player gains food and prestige points proportional to the number of hunters they
 * own: one food per hunter and {@code ppPerPlayer} points per hunter.
 *
 * @see CardStrategy
 */
public class HuntEventStrategy extends CardStrategy {
    // food always +1, the pp change
    private int ppPerPlayer;

    /**
     * Creates a new hunt event effect.
     *
     * @param ppPerPlayer  the prestige points granted per hunter owned
     * @param registration the registration action attaching this effect to the hunter event channel
     */
    public HuntEventStrategy(int ppPerPlayer, RegistrationAction registration) {
        super(registration);
        this.ppPerPlayer = ppPerPlayer;
    }

    /**
     * Grants every player food and prestige points based on the hunters they own.
     *
     * @param playerContext the context granting access to all players
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getPlayers()
                .forEach(p -> {
                    int hunterCount = p.getTribe().countByType(CharacterType.HUNTER);
                    p.updateFood(hunterCount);
                    p.updatePoints(ppPerPlayer * hunterCount);
                });
    }
}
