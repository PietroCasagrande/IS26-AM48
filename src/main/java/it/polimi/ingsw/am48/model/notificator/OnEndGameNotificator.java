package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;
import java.util.Map;

public class OnEndGameNotificator {
    private Map<Player, List<CardStrategy>> listeners;

    public void attach(Player p, CardStrategy cs){
        // TODO
    }

    public void notifyListeners(){
        // TODO
    }
}
