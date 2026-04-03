package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
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
        dtoTwoPlus.id = "char-1";
        dtoTwoPlus.era = "FIRST";
        dtoTwoPlus.character = "PICKER";
        dtoTwoPlus.minPlayers = 2;
        dtoTwoPlus.strategy = null;

        dtoThreePlus = new CardDTO();
        dtoThreePlus.id = "char-2";
        dtoThreePlus.era = "SECOND";
        dtoThreePlus.character = "ARTIST";
        dtoThreePlus.minPlayers = 3;
        dtoThreePlus.strategy = null;

        dtoFivePlus = new CardDTO();
        dtoFivePlus.id = "char-3";
        dtoFivePlus.era = "THIRD";
        dtoFivePlus.character = "INVENTOR";
        dtoFivePlus.minPlayers = 5;
        dtoFivePlus.strategy = null;
    }

    // ==================== createCards ====================

    @Test
    @DisplayName("createCards: should include cards whose minPlayers is equal to numPlayers")
    void shouldIncludeCardWithMinPlayersEqualToNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        List<CharacterCard> result = factory.createCards(2);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("createCards: should include cards whose minPlayers is less than numPlayers")
    void shouldIncludeCardWithMinPlayersBelowNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        List<CharacterCard> result = factory.createCards(4);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("createCards: should exclude cards whose minPlayers exceeds numPlayers")
    void shouldExcludeCardWithMinPlayersAboveNumPlayers() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoFivePlus));
        List<CharacterCard> result = factory.createCards(3);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createCards: should filter correctly when DTOs have mixed minPlayers values")
    void shouldFilterMixedMinPlayersCorrectly() {
        // dtoTwoPlus (min=2) and dtoThreePlus (min=3) should be included; dtoFivePlus (min=5) excluded
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus, dtoThreePlus, dtoFivePlus));
        List<CharacterCard> result = factory.createCards(3);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        CharacterCard card = factory.createCards(2).get(0);
        assertEquals("char-1", card.getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        CharacterCard card = factory.createCards(2).get(0);
        assertEquals(Era.FIRST, card.getEra());
    }

    @Test
    @DisplayName("createCards: should map minPlayers correctly from DTO")
    void shouldMapMinPlayersCorrectly() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        CharacterCard card = factory.createCards(2).get(0);
        assertEquals(2, card.getMinPlayers());
    }

    @Test
    @DisplayName("createCards: should set strategy to null when DTO has no strategy")
    void shouldSetNullStrategyWhenDtoStrategyIsNull() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        CharacterCard card = factory.createCards(2).get(0);
        assertNull(card.getStrategy());
    }

    @Test
    @DisplayName("createCards: should return empty list when all cards are filtered out")
    void shouldReturnEmptyListWhenAllCardsFiltered() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoFivePlus));
        List<CharacterCard> result = factory.createCards(2);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(6));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        dtoTwoPlus.era = "INVALID_ERA";
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown character type")
    void shouldThrowForUnknownCharacterType() {
        dtoTwoPlus.character = "INVALID_TYPE";
        CharacterFactory factory = new CharacterFactory(List.of(dtoTwoPlus));
        assertThrows(IllegalArgumentException.class, () -> factory.createCards(2));
    }
}