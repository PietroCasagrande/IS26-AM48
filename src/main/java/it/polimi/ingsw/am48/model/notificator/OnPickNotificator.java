package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnPickNotificator {
    private Map<Player, List<CardStrategy>> listeners;

    public void attach(Player p, CardStrategy cs){
        listeners.computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    public void detach(Player p, CardStrategy cs){
        if (listeners.get(p) == null || !listeners.get(p).contains(cs)) {
            throw new StrategyNotFoundException("Impossibile rimuovere la strategia: " + cs + " per il giocatore " + p);
        }
        cs.unregister(this,p);
    }

    public void notifyListeners(Player p){
        throw new UnsupportedOperationException("TODO");    }
}
