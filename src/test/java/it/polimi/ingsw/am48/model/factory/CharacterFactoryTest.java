package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacterFactoryTest {

    private CardDTO dtoTwoPlus;
    private CardDTO dtoThreePlus;
    private CardDTO dtoFivePlus;

    @BeforeEach
    void setUp() {
        dtoTwoPlus = new CardDTO();
        dtoTwoPlus.id = "PIC-01";
        dtoTwoPlus.era = "FIRST";
        dtoTwoPlus.character = "PICKER";
        dtoTwoPlus.minPlayers = 2;
        dtoTwoPlus.strategy = null;

        dtoThreePlus = new CardDTO();
        dtoThreePlus.id = "ART-13";
        dtoThreePlus.era = "SECOND";
        dtoThreePlus.character = "ARTIST";
        dtoThreePlus.minPlayers = 3;
        dtoThreePlus.strategy = null;

        dtoFivePlus = new CardDTO();
        dtoFivePlus.id = "INV-19";
        dtoFivePlus.era = "THIRD";
        dtoFivePlus.character = "INVENTOR";
        dtoFivePlus.minPlayers = 5;
        dtoFivePlus.strategy = null;
    }

    // ==================== createCards - filtering ====================

    @Test
    @DisplayName("createCards: should include card when minPlayers equals numPlayers")
    void shouldIncludeCardWithMinPlayersEqualToNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus, dtoThreePlus, dtoFivePlus));
        assertEquals(1, factory.createCards(2).size());
    }

    @Test
    @DisplayName("createCards: should include card when minPlayers is less than numPlayers")
    void shouldIncludeCardWithMinPlayersBelowNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus, dtoThreePlus, dtoFivePlus));
        assertEquals(2, factory.createCards(4).size());
    }

    @Test
    @DisplayName("createCards: should exclude card when minPlayers exceeds numPlayers")
    void shouldExcludeCardWithMinPlayersAboveNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoThreePlus, dtoFivePlus));
        assertTrue(factory.createCards(2).isEmpty());
    }

    @Test
    @DisplayName("createCards: should filter correctly with mixed minPlayers values")
    void shouldFilterMixedMinPlayersCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus, dtoThreePlus, dtoFivePlus));
        assertEquals(2, factory.createCards(3).size());
    }

    // ==================== createCards - mapping ====================

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertEquals("PIC-01", factory.createCards(2).getFirst().getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertEquals(Era.FIRST, factory.createCards(2).getFirst().getEra());
    }

    @Test
    @DisplayName("createCards: should map minPlayers correctly from DTO")
    void shouldMapMinPlayersCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertEquals(2, factory.createCards(2).getFirst().getMinPlayers());
    }

    @Test
    @DisplayName("createCards: should set strategy to null when DTO has no strategy")
    void shouldSetNullStrategyWhenDtoStrategyIsNull() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertNull(factory.createCards(2).getFirst().getStrategy());
    }

    @Test
    @DisplayName("createCards: should return empty list when all cards are filtered out")
    void shouldReturnEmptyListWhenAllCardsFiltered() {
        assertTrue(new CharacterFactory(List.of(dtoFivePlus)).createCards(2).isEmpty());
    }

    // ==================== createCards - validation ====================

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterFactory(List.of(dtoTwoPlus)).createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterFactory(List.of(dtoTwoPlus)).createCards(6));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        dtoTwoPlus.era = "INVALID_ERA";
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterFactory(List.of(dtoTwoPlus)).createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown character type")
    void shouldThrowForUnknownCharacterType() {
        dtoTwoPlus.character = "INVALID_TYPE";
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterFactory(List.of(dtoTwoPlus)).createCards(2));
    }

    // ==================== buildStrategy - ogni ramo del switch ====================

    @Test
    @DisplayName("buildStrategy: should build BuilderStrategy correctly")
    void shouldBuildBuilderStrategy() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "BuilderStrategy";
        s.num1 = 2;
        s.num2 = 1;
        dtoTwoPlus.strategy = s;
        CharacterCard card = new CharacterFactory(List.of(dtoTwoPlus)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build InventorStrategy correctly")
    void shouldBuildInventorStrategy() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "InventorStrategy";
        s.artifact = "ARROW";
        dtoTwoPlus.strategy = s;
        CharacterCard card = new CharacterFactory(List.of(dtoTwoPlus)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ResourcePerCharStrategy correctly")
    void shouldBuildResourcePerCharStrategy() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "ResourcePerCharStrategy";
        s.resource = "FOOD";
        s.character = "HUNTER";
        s.num1 = 1;
        dtoTwoPlus.strategy = s;
        CharacterCard card = new CharacterFactory(List.of(dtoTwoPlus)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build UpdateResourcesStrategy correctly")
    void shouldBuildUpdateResourcesStrategy() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "UpdateResourcesStrategy";
        s.resource = "STAR";
        s.num1 = 3;
        dtoTwoPlus.strategy = s;
        CharacterCard card = new CharacterFactory(List.of(dtoTwoPlus)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should throw IllegalArgumentException for unknown strategy effect")
    void shouldThrowForUnknownStrategyEffect() {
        StrategyDTO s = new StrategyDTO();
        s.effect = "UnknownStrategy";
        dtoTwoPlus.strategy = s;
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterFactory(List.of(dtoTwoPlus)).createCards(2));
    }
}