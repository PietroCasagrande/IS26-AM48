package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.strategy.*;

import java.util.ArrayList;
import java.util.List;

public class EventFactory implements BoardFactory<EventCard> {
    private final List<CardDTO> events;

    public EventFactory(List<CardDTO> events) {
        this.events = events;
    }

    @Override
    public List<EventCard> createCards(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<EventCard> eventDeck = new ArrayList<>();

        for (CardDTO dto : this.events) {
            Era era = Era.valueOf(dto.era);
            EventType eventType = EventType.valueOf(dto.eventType);

            CardStrategy strategy = null;
            if (dto.strategy != null) strategy = buildStrategy(dto.strategy);
            else throw new IllegalArgumentException("Event strategy is not present");

            eventDeck.add(new EventCard(dto.id, era, strategy, eventType));
        }

        return eventDeck;
    }

    //to do
    private CardStrategy buildStrategy(StrategyDTO dto) {
        switch (dto.effect) {
            case "ArtistEventStrategy": {
                RegistrationAction reg = (nc, pc, s) -> {nc.getEventNotificator().attach(EventType.ARTIST_EVENT, s);};
                return new ArtistEventStrategy(dto.num1, dto.num2, dto.num3, reg);
            }
            case "HuntEventStrategy": {
                RegistrationAction reg = (nc, pc, s) -> {nc.getEventNotificator().attach(EventType.HUNTER_EVENT, s);};
                return new HuntEventStrategy(dto.num1, reg);
            }
            case "ShamanEventStrategy": {
                RegistrationAction reg = (nc, pc, s) -> {nc.getEventNotificator().attach(EventType.SHAMAN_EVENT, s);};
                return new ShamanEventStrategy(dto.num1, dto.num2, reg);
            }
            case "SustenanceStrategy": {
                RegistrationAction reg = (nc, pc, s) -> {nc.getEventNotificator().attach(EventType.PICKER_EVENT, s);};
                return new SustenanceStrategy(dto.num1, reg);
            }
            default:
                throw new IllegalArgumentException("Invalid event strategy");
        }
    }
}
