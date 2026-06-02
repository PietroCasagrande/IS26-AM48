package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notificator responsible for triggering end-game scoring strategies
 * for every player in the game. The strategies are evaluated
 * after the final turn, when the game transitions to the
 * {@link it.polimi.ingsw.am48.model.phase.EndGamePhase}.
 */
public class OnEndGameNotificator {
    private Map<Player, List<CardStrategy>> listeners = new HashMap<>();

    public void attach(Player p, CardStrategy cs){
        listeners.computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    /**
     * Executes all registered end-game strategies for every player.
     * Iterates over all players in the context, sets each as the current player,
     * and applies their registered strategies.
     *
     * @param playerContext the context containing all players in the game
     */
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
