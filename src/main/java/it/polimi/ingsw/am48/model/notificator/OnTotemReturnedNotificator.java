package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.HashMap;
import java.util.Map;

public class OnTotemReturnedNotificator {
    private Map<Player, CardStrategy> listeners = new HashMap<>();

    public void attach(Player p, CardStrategy cs) {
        listeners.put(p, cs);
    }

    public void notify(PlayerContext p) {
        // taking the current player
        Player currPlayer = p.getCurrPlayer();

        // looking for the strategy
        CardStrategy strategy = listeners.get(currPlayer);

        // if present, it activates the effect
        if (strategy != null) {
            strategy.effect(p);
        }
    }
}
