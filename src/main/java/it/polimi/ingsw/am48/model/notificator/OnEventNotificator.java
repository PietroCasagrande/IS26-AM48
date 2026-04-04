package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnEventNotificator {
    private Map<EventType, Map<Player,List<CardStrategy>>> listeners = new HashMap<>();;

    public void attach(EventType e, Player p, CardStrategy cs) {
        listeners.computeIfAbsent(e, k -> new HashMap<>())
                .computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    public void detach(EventType e, Player p, CardStrategy cs){
        Map<Player, List<CardStrategy>> playerMap = listeners.get(e);

        if (playerMap == null) {
            throw new StrategyNotFoundException("Impossibile rimuovere la strategia: " + cs + " per il giocatore " + p);
        }

        List<CardStrategy> strategies = playerMap.get(p);

        if (strategies == null || !strategies.contains(cs)) {
            throw new StrategyNotFoundException("Impossibile rimuovere la strategia: " + cs + " per il giocatore " + p);
        }

        strategies.remove(cs);
        if (strategies.isEmpty()) playerMap.remove(p);
        if (playerMap.isEmpty()) listeners.remove(e);
    }

    public void notify(EventType e, PlayerContext playerContext) {
        Map<Player, List<CardStrategy>> playerMap = listeners.get(e);
        if (playerMap == null) return;

        Player previous = playerContext.getCurrPlayer();

        for (Map.Entry<Player, List<CardStrategy>> entry : playerMap.entrySet()) {
            playerContext.setCurrPlayer(entry.getKey());

            List<CardStrategy> toDetach = new ArrayList<>();

            for (CardStrategy cs : entry.getValue()) {
                cs.effect(playerContext);
                cs.unregisterFrom(toDetach);
            }

            entry.getValue().removeAll(toDetach);
        }
        // resets the previous player as the current
        playerContext.setCurrPlayer(previous);
    }
}
