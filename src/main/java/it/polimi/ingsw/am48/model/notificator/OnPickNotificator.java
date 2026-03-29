package it.polimi.ingsw.am48.model.notificator;

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




    public void detach(){
        throw new UnsupportedOperationException("TODO");    }

    public void notifyListeners(Player p){
        throw new UnsupportedOperationException("TODO");    }
}
