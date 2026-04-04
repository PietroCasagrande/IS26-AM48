package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
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

    public void detach(){
        throw new UnsupportedOperationException("TODO");    }

    public void notifyListeners(EventType p){
        throw new UnsupportedOperationException("TODO");    }
}
