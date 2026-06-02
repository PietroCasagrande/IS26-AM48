package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Notificator responsible for triggering strategies when the current
 * player's totem is returned from the offer track at the end of a turn.
 * This is a single-use notificator that holds at most one entry, the
 * {@link it.polimi.ingsw.am48.model.strategy.ExtraFoodOnFoodStrategy}.
 */
public class OnTotemReturnedNotificator {
    private Map<Player, CardStrategy> listeners = new HashMap<>();

    /**
     * Registers a strategy to be executed when the specified player's
     * totem is returned. Since this notificator supports at most
     * one listener, a subsequent call replaces the previous one.
     *
     * @param p the player who owns the card that registered this strategy
     * @param cs the strategy to execute on totem return
     */
    public void attach(Player p, CardStrategy cs) {
        listeners.put(p, cs);
    }

    /**
     * Executes the registered strategy when the current player's totem
     * is returned from the offer track. If no strategy is registered for
     * the current player, this method does nothing.
     *
     * @param p the context containing all players; the current player's
     * strategy is looked up and executed
     */
    public void notify(PlayerContext p) {
        // retrieves the currently active player
        Player currPlayer = p.getCurrPlayer();

        // looks up the strategy registered for this player
        CardStrategy strategy = listeners.get(currPlayer);

        // executes the strategy if one was registered
        if (strategy != null) {
            strategy.effect(p);
        }
    }
}
