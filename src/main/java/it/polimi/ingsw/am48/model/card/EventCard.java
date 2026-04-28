package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

public class EventCard extends Card{
    private final EventType eventType;

    public EventCard(String cardId, Era era, CardStrategy strategy, EventType eventType) {
        super(cardId, era, strategy);
        this.eventType = eventType;
    }

    // getter() per il tipo di evento
    public EventType getEventType() { return eventType; }

    @Override
    public void acquire(Player player) {
        // Le EventCard non vengono acquisite dal giocatore
        throw new InvalidActionException("EventCard can not be acquired.");
    }
}
