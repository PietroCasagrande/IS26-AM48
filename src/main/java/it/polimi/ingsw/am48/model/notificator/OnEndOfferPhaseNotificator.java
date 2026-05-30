package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Notificator responsible for triggering a post-offer-phase strategy
 * after the offer (picking) phase ends. This is a single-use notificator
 * that holds at most one entry, the
 * {@link it.polimi.ingsw.am48.model.strategy.ExtraPickStrategy}, which grants
 * an additional pick to the player who built the right building.
 */

public class OnEndOfferPhaseNotificator {
    private Map<Player, CardStrategy> listeners = new HashMap<>();

    /**
     * Registers a strategy to be executed at the end of the offer phase
     * for the specified player. Since this notificator supports at most
     * one listener, a subsequent call replaces the previous one.
     *
     * @param p  the player who owns the card that registered this strategy
     * @param cs the strategy to execute at the end of the offer phase
     */

    public void attach(Player p, CardStrategy cs) {
        listeners.put(p, cs);
    }

    /**
     * Executes the registered strategy at the end of the offer phase.
     * Temporarily sets the card owner as the current player before executing
     * the strategy, then restores the previous current player.
     *
     * @param playerContext the context containing all players in the game
     */

    public void notify(PlayerContext playerContext) {
        if (listeners.isEmpty()) return;

        // at most one entry
        Map.Entry<Player, CardStrategy> entry = listeners.entrySet().iterator().next();

        Player cardOwner = entry.getKey();
        CardStrategy strategy = entry.getValue();

        // saves the currently active player before switching context
        Player previous = playerContext.getCurrPlayer();

        // switches context to the card owner so the strategy applies to the right player
        playerContext.setCurrPlayer(cardOwner);

        strategy.effect(playerContext);

        // restores the previous player as the current one
        playerContext.setCurrPlayer(previous);
    }
}
