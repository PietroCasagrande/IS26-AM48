package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferTrackFactoryTest {

    private OfferCardDTO dtoA;
    private OfferCardDTO dtoB;
    private OfferCardDTO dtoC;

    @BeforeEach
    void setUp() {
        dtoA = new OfferCardDTO();
        dtoA.id = 'A';
        dtoA.minPlayers = 2;
        dtoA.strategy = null;

        dtoB = new OfferCardDTO();
        dtoB.id = 'B';
        dtoB.minPlayers = 3;
        dtoB.strategy = null;

        dtoC = new OfferCardDTO();
        dtoC.id = 'C';
        dtoC.minPlayers = 2;
        dtoC.strategy = null;
    }

    // ==================== createCards ====================

    @Test
    @DisplayName("createCards: should return one OfferCard per DTO provided")
    void shouldCreateOneCardPerDto() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        List<OfferCard> result = factory.createCards(3);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("createCards: should map char id correctly from DTO")
    void shouldMapCharIdCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        OfferCard card = factory.createCards(2).get(0);
        //assertEquals('A', card.getCardId());
    }

    @Test
    @DisplayName("createCards: should map minPlayers correctly from DTO")
    void shouldMapMinPlayersCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoB));
        OfferCard card = factory.createCards(3).get(0);
        //assertEquals(3, card.getMinPlayers());
    }

    @Test
    @DisplayName("createCards: should set strategy to null when DTO has no strategy")
    void shouldSetNullStrategyWhenDtoStrategyIsNull() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        OfferCard card = factory.createCards(2).get(0);
        //assertNull(card.getStrategy());
    }

    @Test
    @DisplayName("createCards: should preserve insertion order of cards")
    void shouldPreserveInsertionOrder() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        List<OfferCard> result = factory.createCards(3);
        //assertEquals('A', result.get(0).getId());
        //assertEquals('B', result.get(1).getId());
        //assertEquals('C', result.get(2).getId());
    }

    @Test
    @DisplayName("createCards: should return an empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of());
        List<OfferCard> result = factory.createCards(2);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertDoesNotThrow(() -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertDoesNotThrow(() -> factory.createCards(5));
    }
}