package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;
import java.util.Map;

public class OnTotemReturnedNotificator {
    private Map<Player, CardStrategy> listeners;

    public void attach(Player p, CardStrategy cs){
        throw new UnsupportedOperationException("TODO");    }

    public void notifyListeners(){
        throw new UnsupportedOperationException("TODO");    }
}
