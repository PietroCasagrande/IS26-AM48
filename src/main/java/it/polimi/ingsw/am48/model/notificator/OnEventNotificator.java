package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.*;

public class OnEventNotificator {
    private final Map<EventType, Map<Player,List<CardStrategy>>> buildingListeners = new EnumMap<>(EventType.class);
    private final Map<EventType, List<CardStrategy>> eventListeners = new EnumMap<>(EventType.class);

    // Gli EventType dei metodi attach saranno passati dalle lambda function
    // Attaches event-depending buildings (persistent)
    public void attach(EventType e, Player p, CardStrategy cs) {
        buildingListeners.computeIfAbsent(e, k -> new HashMap<>())
                .computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    // Attaches events (one-shot)
    public void attach(EventType e, CardStrategy cs) {
        eventListeners.computeIfAbsent(e, k -> new ArrayList<>())
                .add(cs);
    }

    public void notify(EventType e, PlayerContext playerContext) {
        // If an event is not present for the current turn, doesn't activate any effect
        if (!this.eventListeners.containsKey(e)) return;

        // Activates building effect
        Map<Player, List<CardStrategy>> playerMap = buildingListeners.get(e);
        if (playerMap != null) {
            Player previous = playerContext.getCurrPlayer();

            for (Map.Entry<Player, List<CardStrategy>> entry : playerMap.entrySet()) {
                playerContext.setCurrPlayer(entry.getKey());
                for (CardStrategy cs : entry.getValue()) cs.effect(playerContext);
            }
            // resets the previous player as the current
            playerContext.setCurrPlayer(previous);
        }

        // Activates event effect and then deletes them
        for (CardStrategy cs : this.eventListeners.get(e)) cs.effect(playerContext);
        this.eventListeners.get(e).clear();
    }
}
