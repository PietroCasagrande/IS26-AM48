package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.HashMap;
import java.util.Map;

public class OnEndOfferPhaseNotificator {
    private Map<Player, CardStrategy> listeners = new HashMap<>();

    public void attach(Player p, CardStrategy cs) {
        listeners.put(p, cs);
    }

    public void notify(PlayerContext playerContext) {
        if (listeners.isEmpty()) return;

        // at most one entry
        Map.Entry<Player, CardStrategy> entry = listeners.entrySet().iterator().next();

        Player cardOwner = entry.getKey();
        CardStrategy strategy = entry.getValue();

        // saves the current player playing
        Player previous = playerContext.getCurrPlayer();

        // sets the owner that has got the card
        playerContext.setCurrPlayer(cardOwner);

        strategy.effect(playerContext);

        // sets the previous player
        playerContext.setCurrPlayer(previous);
    }
}
