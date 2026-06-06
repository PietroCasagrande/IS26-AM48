package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.strategy.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete factory responsible for instantiating {@link EventCard} objects.
 * <p>
 * This factory parses a list of data transfer objects (DTOs) and builds the game's event deck.
 * Event cards intrinsically require an active strategy to function,
 * and their strategies are specifically configured to register themselves as listeners
 * to the event notificator.
 */
public class EventFactory implements BoardFactory<EventCard> {
    private final List<CardDTO> events;

    /**
     * Creates a new {@code EventFactory} initialized with the base event data.
     *
     * @param events a list of {@link CardDTO} containing the blueprints for all available events
     */
    public EventFactory(List<CardDTO> events) {
        this.events = events;
    }

    /**
     * Generates the complete deck of event cards for the game.
     *
     * @param numPlayers the number of players in the game (must be between 2 and 5)
     * @return a fully initialized list of {@link EventCard}
     * @throws IllegalArgumentException if the number of players is outside the valid range (2-5),
     * or if a parsed event DTO lacks a valid strategy definition
     */
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

    /**
     * Constructs the active strategy for an event card and binds its registration logic.
     * <p>
     * Note: Unlike immediate effects, Event strategies provide a specific {@link RegistrationAction}
     * lambda function. This function ensures that when the event must be resolved,
     * it attaches itself to the correct specialized channel of the {@code EventNotificator}).
     *
     * @param dto the strategy blueprint containing the specific effect type and required parameters
     * @return the instantiated {@link CardStrategy} with its specialized registration hook
     * @throws IllegalArgumentException if the effect type specified in the DTO is unknown
     */
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
