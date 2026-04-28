package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnEndGameNotificator {
    private Map<Player, List<CardStrategy>> listeners = new HashMap<>();

    public void attach(Player p, CardStrategy cs){
        listeners.computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }
    public void notify(PlayerContext playerContext) {
        if (listeners.isEmpty()) return;

        for (Player p : playerContext.getPlayers()) {
            List<CardStrategy> strategies = listeners.get(p);
            if (strategies == null) continue;

            playerContext.setCurrPlayer(p);
            for (CardStrategy cs : strategies) {
                cs.effect(playerContext);
            }
        }
    }
}
