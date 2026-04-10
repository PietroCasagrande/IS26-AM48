package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnPickNotificator {
    // private Map<Player, List<CardStrategy>> listeners;
    private Map<Player, List<CardStrategy>> listeners = new HashMap<>();
    public void attach(Player p, CardStrategy cs){
        listeners.computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    public void detach(Player p, CardStrategy cs){
        List<CardStrategy> strategies = listeners.get(p);

        if (strategies == null || !strategies.contains(cs)) {
            throw new StrategyNotFoundException("Impossibile rimuovere la strategia: " + cs + " per il giocatore " + p);
        }
        strategies.remove(cs);
        if (strategies.isEmpty()) listeners.remove(p);
    }

    public void notify(PlayerContext p) {
        List<CardStrategy> strategies = listeners.get(p.getCurrPlayer());
        if (strategies == null) return;
        for (CardStrategy cs : strategies) cs.effect(p);
    }
}
