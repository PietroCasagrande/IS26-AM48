package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notificator responsible for triggering strategies immediately after
 * a card is picked by the current player. Strategies registered here
 * grant immediate effects.
 */
public class OnPickNotificator {
    private Map<Player, List<CardStrategy>> listeners = new HashMap<>();

    /**
     * Registers a strategy to be executed when the specified player picks a card.
     *
     * @param p  the player who owns the card that registered this strategy
     * @param cs the strategy to execute on card pick
     */
    public void attach(Player p, CardStrategy cs){
        listeners.computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    /**
     * Executes all strategies registered for the currently active player
     * immediately after a card is picked.
     *
     * @param p the context containing all players; only the current player's
     *          strategies are executed
     */
    public void notify(PlayerContext p) {
        List<CardStrategy> strategies = listeners.get(p.getCurrPlayer());
        if (strategies == null) return;
        for (CardStrategy cs : strategies) cs.effect(p);
    }
}
