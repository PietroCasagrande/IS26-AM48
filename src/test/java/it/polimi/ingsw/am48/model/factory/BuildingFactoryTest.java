package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.enums.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingFactoryTest {

    private CardDTO dtoFirst;
    private CardDTO dtoSecond;
    private CardDTO dtoThird;

    @BeforeEach
    void setUp() {
        dtoFirst = new CardDTO();
        dtoFirst.id = "BUI-01";
        dtoFirst.era = "FIRST";
        dtoFirst.foodCost = 2;
        dtoFirst.prestigePoints = 3;
        dtoFirst.strategy = null;

        dtoSecond = new CardDTO();
        dtoSecond.id = "BUI-20";
        dtoSecond.era = "SECOND";
        dtoSecond.foodCost = 4;
        dtoSecond.prestigePoints = 5;
        dtoSecond.strategy = null;

        dtoThird = new CardDTO();
        dtoThird.id = "BUI-21";
        dtoThird.era = "THIRD";
        dtoThird.foodCost = 6;
        dtoThird.prestigePoints = 7;
        dtoThird.strategy = null;
    }

    // ==================== createCards ====================

    @Test
    @DisplayName("createCards: should return a BuildingCard for each DTO provided")
    void shouldCreateOneCardPerDto() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst, dtoSecond, dtoThird));
        List<BuildingCard> result = factory.createCards(3);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        BuildingCard card = factory.createCards(2).get(0);
        assertEquals("BUI-01", card.getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        BuildingCard card = factory.createCards(2).get(0);
        assertEquals(Era.FIRST, card.getEra());
    }

    @Test
    @DisplayName("createCards: should map foodCost correctly from DTO")
    void shouldMapFoodCostCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        BuildingCard card = factory.createCards(2).get(0);
        assertEquals(2, card.getFoodCost());
    }

    @Test
    @DisplayName("createCards: should map prestigePoints correctly from DTO")
    void shouldMapPrestigePointsCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        BuildingCard card = factory.createCards(2).get(0);
        assertEquals(3, card.getPrestigePoints());
    }

    @Test
    @DisplayName("createCards: should set strategy to null when DTO has no strategy")
    void shouldSetNullStrategyWhenDtoStrategyIsNull() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        BuildingCard card = factory.createCards(2).get(0);
        assertNull(card.getStrategy());
    }

    @Test
    @DisplayName("createCards: should return an empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        BuildingFactory factory = new BuildingFactory(List.of());
        List<BuildingCard> result = factory.createCards(2);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        assertDoesNotThrow(() -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        assertDoesNotThrow(() -> factory.createCards(5));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        dtoFirst.era = "INVALID_ERA";
        BuildingFactory factory = new BuildingFactory(List.of(dtoFirst));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }
}