package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.HashMap;
import java.util.Map;

public class OnEndOfferPhaseNotificator {
    private Map<Player, CardStrategy> listeners = new HashMap<>();

    public void attach(Player p, CardStrategy cs) {
        listeners.put(p, cs);
    }

    public void notifyListeners(){
        throw new UnsupportedOperationException("TODO");    }
}
