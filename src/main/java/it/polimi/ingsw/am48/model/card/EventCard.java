package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;

public class EventCard extends Card{
    private final EventType eventType;

    public EventCard(String cardId, Era era, CardStrategy strategy, EventType eventType) {
        super(cardId, era, strategy);
        this.eventType = eventType;
    }

    // solito discorso x i getter, capiamo se serve
    // public EventType getEventType() { return eventType; }

    @Override
    public void onPlay(Player player, List<Player> allPlayers) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void acquire(Player player, Showed<Card> showedList) {
        // Le EventCard non vengono acquisite dal giocatore
        // throw new InvalidActionException("Le carte Evento non possono essere acquisite.");
        throw new UnsupportedOperationException("TODO");
    }
}
