package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

/**
 * Represents an Event card within the game.
 * <p>
 * Unlike Characters or Buildings, Event cards are never collected or placed on a
 * player's personal board. Instead, their effects are automatically registered to the
 * notification center when they reach the lower row on the board, and
 * trigger during the end of the next turn. At the end of the game, remaining events
 * are registered and immediately resolved.
 */
public class EventCard extends Card{
    private final EventType eventType;

    /**
     * Constructs a new {@code EventCard} with its specific attributes.
     *
     * @param cardId    the unique string identifier for this card
     * @param era       the chronological era to which this card belongs
     * @param strategy  the active effect associated with this event, which handles its own registration
     * @param eventType the specific category or nature of the event (e.g., SHAMAN_EVENT)
     */
    public EventCard(String cardId, Era era, CardStrategy strategy, EventType eventType) {
        super(cardId, era, strategy);
        this.eventType = eventType;
    }

    /**
     * Rejects any attempt to acquire this card.
     * <p>
     * By the rules of the game domain, events cannot be picked.
     *
     * @param playerContext the context of the player attempting the illegal acquisition
     * @throws InvalidActionException always, as event cards cannot be acquired
     */
    @Override
    public void acquire(PlayerContext playerContext) {
        throw new InvalidActionException("Event cards cannot be acquired");
    }

    public EventType getEventType() { return eventType; }
}
