package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFactoryTest {

    // Helper per costruire un DTO evento con la strategia corretta
    private CardDTO buildHuntEventDto(String id, String era) {
        StrategyDTO s = new StrategyDTO();
        s.effect = "HuntEventStrategy";
        s.num1 = 2;
        CardDTO dto = new CardDTO();
        dto.id = id;
        dto.era = era;
        dto.eventType = "HUNTER_EVENT";
        dto.strategy = s;
        return dto;
    }

    private CardDTO buildArtistEventDto(String id, String era) {
        StrategyDTO s = new StrategyDTO();
        s.effect = "ArtistEventStrategy";
        s.num1 = 2;
        s.num2 = 3;
        s.num3 = 1;
        CardDTO dto = new CardDTO();
        dto.id = id;
        dto.era = era;
        dto.eventType = "ARTIST_EVENT";
        dto.strategy = s;
        return dto;
    }

    private CardDTO buildShamanEventDto(String id, String era) {
        StrategyDTO s = new StrategyDTO();
        s.effect = "ShamanEventStrategy";
        s.num1 = 3;
        s.num2 = 7;
        CardDTO dto = new CardDTO();
        dto.id = id;
        dto.era = era;
        dto.eventType = "SHAMAN_EVENT";
        dto.strategy = s;
        return dto;
    }

    private CardDTO buildSustenanceDto(String id, String era) {
        StrategyDTO s = new StrategyDTO();
        s.effect = "SustenanceStrategy";
        s.num1 = 1;
        CardDTO dto = new CardDTO();
        dto.id = id;
        dto.era = era;
        dto.eventType = "PICKER_EVENT";
        dto.strategy = s;
        return dto;
    }

    // ==================== createCards - validation ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST"))).createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST"))).createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        assertDoesNotThrow(
                () -> new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST"))).createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        assertDoesNotThrow(
                () -> new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST"))).createCards(5));
    }

    // ==================== createCards - missing strategy ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException when DTO has no strategy")
    void shouldThrowWhenDtoHasNoStrategy() {
        CardDTO dto = new CardDTO();
        dto.id = "EVT-02";
        dto.era = "SECOND";
        dto.eventType = "HUNTER_EVENT";
        dto.strategy = null;
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(dto)).createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw for the offending DTO even when other valid DTOs are present")
    void shouldThrowForOffendingDtoEvenWhenOtherDtosAreValid() {
        CardDTO noStrategy = new CardDTO();
        noStrategy.id = "EVT-BAD";
        noStrategy.era = "FIRST";
        noStrategy.eventType = "HUNTER_EVENT";
        noStrategy.strategy = null;
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST"), noStrategy))
                        .createCards(2));
    }

    // ==================== createCards - mapping ====================

    @Test
    @DisplayName("createCards: should return one EventCard per DTO provided")
    void shouldCreateOneCardPerDto() {
        EventFactory factory = new EventFactory(List.of(
                buildHuntEventDto("EVH-01", "FIRST"),
                buildShamanEventDto("EVS-01", "SECOND")));
        assertEquals(2, factory.createCards(2).size());
    }

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        EventFactory factory = new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST")));
        assertEquals("EVH-01", factory.createCards(2).getFirst().getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        EventFactory factory = new EventFactory(List.of(buildHuntEventDto("EVH-01", "SECOND")));
        assertEquals(Era.SECOND, factory.createCards(2).getFirst().getEra());
    }

    @Test
    @DisplayName("createCards: should map eventType correctly from DTO")
    void shouldMapEventTypeCorrectly() {
        EventFactory factory = new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST")));
        assertEquals(EventType.HUNTER_EVENT, factory.createCards(2).getFirst().getEventType());
    }

    @Test
    @DisplayName("createCards: should return empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        assertTrue(new EventFactory(List.of()).createCards(2).isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        CardDTO dto = buildHuntEventDto("EVH-01", "INVALID_ERA");
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(dto)).createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown eventType value")
    void shouldThrowForUnknownEventTypeValue() {
        CardDTO dto = buildHuntEventDto("EVH-01", "FIRST");
        dto.eventType = "INVALID_EVENT_TYPE";
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(dto)).createCards(2));
    }

    // ==================== buildStrategy - ogni ramo del switch ====================

    @Test
    @DisplayName("buildStrategy: should build ArtistEventStrategy correctly")
    void shouldBuildArtistEventStrategy() {
        EventCard card = new EventFactory(List.of(buildArtistEventDto("EVA-01", "FIRST")))
                .createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build HuntEventStrategy correctly")
    void shouldBuildHuntEventStrategy() {
        EventCard card = new EventFactory(List.of(buildHuntEventDto("EVH-01", "FIRST")))
                .createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ShamanEventStrategy correctly")
    void shouldBuildShamanEventStrategy() {
        EventCard card = new EventFactory(List.of(buildShamanEventDto("EVS-01", "SECOND")))
                .createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build SustenanceStrategy correctly")
    void shouldBuildSustenanceStrategy() {
        EventCard card = new EventFactory(List.of(buildSustenanceDto("EVP-01", "FIRST")))
                .createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should throw IllegalArgumentException for unknown strategy effect")
    void shouldThrowForUnknownStrategyEffect() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "UnknownEventStrategy";
        CardDTO dto = new CardDTO();
        dto.id = "EVT-XX";
        dto.era = "FIRST";
        dto.eventType = "HUNTER_EVENT";
        dto.strategy = s;
        assertThrows(IllegalArgumentException.class,
                () -> new EventFactory(List.of(dto)).createCards(2));
    }
}