package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferTurnCardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferTurnFactoryTest {

    private OfferTurnCardDTO dto2;
    private OfferTurnCardDTO dto3;
    private OfferTurnCardDTO dto4;
    private OfferTurnCardDTO dto5;
    private List<OfferTurnCardDTO> allDtos;

    @BeforeEach
    void setUp() {
        dto2 = new OfferTurnCardDTO();
        dto2.numPlayers = 2;
        dto2.foodRewards = List.of(1, 0, 1);
        dto2.ppPenalty = 2;

        dto3 = new OfferTurnCardDTO();
        dto3.numPlayers = 3;
        dto3.foodRewards = List.of(2, 0, 1);
        dto3.ppPenalty = 2;

        dto4 = new OfferTurnCardDTO();
        dto4.numPlayers = 4;
        dto4.foodRewards = List.of(2, 1, 1);
        dto4.ppPenalty = 2;

        dto5 = new OfferTurnCardDTO();
        dto5.numPlayers = 5;
        dto5.foodRewards = List.of(3, 1, 1);
        dto5.ppPenalty = 2;

        allDtos = List.of(dto2, dto3, dto4, dto5);
    }

    // ==================== createCards - validation ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertDoesNotThrow(() -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertDoesNotThrow(() -> factory.createCards(5));
    }

    // ==================== createCards - filtering ====================

    @Test
    @DisplayName("createCards: should return exactly one card matching the requested numPlayers")
    void shouldReturnExactlyOneCardMatchingNumPlayers() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertEquals(1, factory.createCards(3).size());
    }

    @Test
    @DisplayName("createCards: should return empty list when no DTO matches numPlayers")
    void shouldReturnEmptyListWhenNoDtoMatchesNumPlayers() {
        // List with only dto2: asking for 4 players yields no match
        OfferTurnFactory factory = new OfferTurnFactory(List.of(dto2));
        assertTrue(factory.createCards(4).isEmpty());
    }

    @Test
    @DisplayName("createCards: should return empty list when DTO list is empty")
    void shouldReturnEmptyListWhenDtoListIsEmpty() {
        OfferTurnFactory factory = new OfferTurnFactory(List.of());
        assertTrue(factory.createCards(3).isEmpty());
    }

    @Test
    @DisplayName("createCards: should return multiple cards if multiple DTOs match numPlayers")
    void shouldReturnMultipleCardsIfMultipleDtosMatchNumPlayers() {
        // Two DTOs both with numPlayers=3
        OfferTurnCardDTO extraDto3 = new OfferTurnCardDTO();
        extraDto3.numPlayers = 3;
        extraDto3.foodRewards = List.of(1, 1, 0);
        extraDto3.ppPenalty = 1;

        OfferTurnFactory factory = new OfferTurnFactory(List.of(dto3, extraDto3));
        assertEquals(2, factory.createCards(3).size());
    }

    // ==================== createCards - mapping ====================

    @Test
    @DisplayName("createCards: should map numPlayers correctly from matching DTO")
    void shouldMapNumPlayersCorrectly() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertEquals(3, factory.createCards(3).get(0).getNumPlayers());
    }

    @Test
    @DisplayName("createCards: should map foodRewards correctly from matching DTO")
    void shouldMapFoodRewardsCorrectly() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertEquals(List.of(2, 0, 1), factory.createCards(3).get(0).getFoodRewards());
    }

    @Test
    @DisplayName("createCards: should map ppPenalty correctly from matching DTO")
    void shouldMapPpPenaltyCorrectly() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);
        assertEquals(2, factory.createCards(3).get(0).getPpPenalty());
    }

    @Test
    @DisplayName("createCards: should correctly map each player count to its own DTO")
    void shouldCorrectlyMapEachPlayerCountToItsOwnDto() {
        OfferTurnFactory factory = new OfferTurnFactory(allDtos);

        assertEquals(List.of(1, 0, 1), factory.createCards(2).get(0).getFoodRewards());
        assertEquals(List.of(2, 0, 1), factory.createCards(3).get(0).getFoodRewards());
        assertEquals(List.of(2, 1, 1), factory.createCards(4).get(0).getFoodRewards());
        assertEquals(List.of(3, 1, 1), factory.createCards(5).get(0).getFoodRewards());
    }
}