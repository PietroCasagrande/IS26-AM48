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
        dtoA.numUp = 1;
        dtoA.numDown = 0;
        dtoA.foodBonus = 3;

        dtoB = new OfferCardDTO();
        dtoB.id = 'B';
        dtoB.minPlayers = 3;
        dtoB.numUp = 0;
        dtoB.numDown = 1;
        dtoB.foodBonus = 0;

        dtoC = new OfferCardDTO();
        dtoC.id = 'C';
        dtoC.minPlayers = 2;
        dtoC.numUp = 1;
        dtoC.numDown = 1;
        dtoC.foodBonus = 0;
    }

    // ==================== createCards ====================

    @Test
    @DisplayName("createCards: should return one OfferCard per DTO provided")
    void shouldCreateOneCardPerDto() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        assertEquals(3, factory.createCards(3).size());
    }

    @Test
    @DisplayName("createCards: should map letterId correctly from DTO")
    void shouldMapLetterIdCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertEquals('A', factory.createCards(2).getFirst().getLetterId());
    }

    @Test
    @DisplayName("createCards: should map minPlayers correctly from DTO")
    void shouldMapMinPlayersCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoB));
        assertEquals(3, factory.createCards(3).getFirst().getNumPlayers());
    }

    @Test
    @DisplayName("createCards: should map numUp correctly from DTO")
    void shouldMapNumUpCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertEquals(1, factory.createCards(2).getFirst().getNumUp());
    }

    @Test
    @DisplayName("createCards: should map numDown correctly from DTO")
    void shouldMapNumDownCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoB));
        assertEquals(1, factory.createCards(3).getFirst().getNumDown());
    }

    @Test
    @DisplayName("createCards: should map foodBonus correctly from DTO")
    void shouldMapFoodBonusCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertEquals(3, factory.createCards(2).getFirst().getFoodBonus());
    }

    @Test
    @DisplayName("createCards: should preserve insertion order of cards")
    void shouldPreserveInsertionOrder() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        List<OfferCard> result = factory.createCards(3);
        assertEquals('A', result.get(0).getLetterId());
        assertEquals('B', result.get(1).getLetterId());
        assertEquals('C', result.get(2).getLetterId());
    }

    @Test
    @DisplayName("createCards: should return an empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of());
        assertTrue(factory.createCards(2).isEmpty());
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