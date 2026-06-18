package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.*;

/**
 * Notificator responsible for triggering strategies when an event card
 * is resolved during the end-of-turn phase. It maintains two separate
 * listener maps to distinguish between two types:
 * <ul>
 *   <li><b>Building listeners</b> ({@code buildingListeners}) — persistent
 *       strategies tied to a specific player, triggered each time the
 *       associated event type fires.</li>
 *   <li><b>Event listeners</b> ({@code eventListeners}) — one-shot strategies
 *       triggered once and then cleared after execution. These correspond
 *       to event cards drawn from the event deck.</li>
 * </ul>
 */

public class OnEventNotificator {
    private final Map<EventType, Map<Player,List<CardStrategy>>> buildingListeners = new EnumMap<>(EventType.class);
    private final Map<EventType, List<CardStrategy>> eventListeners = new EnumMap<>(EventType.class);

    /**
     * Registers a player-specific, persistent strategy for the given event type.
     * The strategy is triggered every time the event is resolved.
     *
     * @param e the event type that triggers this strategy
     * @param p the player who owns the building that grants this strategy
     * @param cs the strategy to execute when the event fires
     */

    // Attaches event-depending buildings (persistent)
    public void attach(EventType e, Player p, CardStrategy cs) {
        buildingListeners.computeIfAbsent(e, k -> new HashMap<>())
                .computeIfAbsent(p, k -> new ArrayList<>())
                .add(cs);
    }

    /**
     * Registers a one-shot strategy for the given event type.
     * The strategy is executed once when the event fires and then automatically removed.
     *
     * @param e the event type that triggers this strategy
     * @param cs the strategy to execute when the event fires
     */

    // Attaches events (one-shot)
    public void attach(EventType e, CardStrategy cs) {
        eventListeners.computeIfAbsent(e, k -> new ArrayList<>())
                .add(cs);
    }

    /**
     * Notifies all strategies registered for the given event type.
     * First executes persistent building strategies for each owning player,
     * then executes and clears one-shot event strategies.
     *
     * @param e the event type to fire
     * @param playerContext the context containing all players in the game
     */
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
