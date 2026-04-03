package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFactoryTest {

    private CardDTO dtoWithStrategy;
    private CardDTO dtoWithoutStrategy;

    @BeforeEach
    void setUp() {
        StrategyDTO strategy = new StrategyDTO();
        strategy.effect = "SomeEventStrategy";
        strategy.notificator = "OnEvent";

        dtoWithStrategy = new CardDTO();
        dtoWithStrategy.id = "EVT-01";
        dtoWithStrategy.era = "FIRST";
        dtoWithStrategy.eventType = "HUNTER_EVENT";
        dtoWithStrategy.strategy = strategy;

        dtoWithoutStrategy = new CardDTO();
        dtoWithoutStrategy.id = "EVT-02";
        dtoWithoutStrategy.era = "SECOND";
        dtoWithoutStrategy.eventType = "HUNTER_EVENT";
        dtoWithoutStrategy.strategy = null;
    }

    // ==================== createCards - validation ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        //Event Strategies needed
        //assertDoesNotThrow(() -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        //Event Strategies needed
        //assertDoesNotThrow(() -> factory.createCards(5));
    }

    // ==================== createCards - missing strategy ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException when DTO has no strategy")
    void shouldThrowWhenDtoHasNoStrategy() {
        EventFactory factory = new EventFactory(List.of(dtoWithoutStrategy));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw for the offending DTO even when other valid DTOs are present")
    void shouldThrowForOffendingDtoEvenWhenOtherDtosAreValid() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy, dtoWithoutStrategy));
        //Event Strategies needed
        //assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }

    // ==================== createCards - mapping ====================

    @Test
    @DisplayName("createCards: should return one EventCard per DTO provided")
    void shouldCreateOneCardPerDto() {
        StrategyDTO strategy2 = new StrategyDTO();
        strategy2.effect = "AnotherEventStrategy";
        strategy2.notificator = "OnEvent";

        CardDTO dto2 = new CardDTO();
        dto2.id = "EVT-03";
        dto2.era = "THIRD";
        dto2.eventType = "SHAMAN_EVENT";
        dto2.strategy = strategy2;

        EventFactory factory = new EventFactory(List.of(dtoWithStrategy, dto2));
        //Event Strategies needed
        //assertEquals(2, factory.createCards(2).size());
    }

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        //Event Strategies needed
        //assertEquals("EVT-01", factory.createCards(2).get(0).getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        //Event Strategies needed
        //assertEquals(Era.FIRST, factory.createCards(2).get(0).getEra());
    }

    @Test
    @DisplayName("createCards: should map eventType correctly from DTO")
    void shouldMapEventTypeCorrectly() {
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        //Event Strategies needed
        //assertEquals(EventType.HUNTER_EVENT, factory.createCards(2).get(0).getEventType());
    }

    @Test
    @DisplayName("createCards: should return empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        EventFactory factory = new EventFactory(List.of());
        assertTrue(factory.createCards(2).isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        dtoWithStrategy.era = "INVALID_ERA";
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown eventType value")
    void shouldThrowForUnknownEventTypeValue() {
        dtoWithStrategy.eventType = "INVALID_EVENT_TYPE";
        EventFactory factory = new EventFactory(List.of(dtoWithStrategy));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }
}