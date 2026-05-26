package it.polimi.ingsw.am48.utils;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.dto.OfferTurnCardDTO;
import it.polimi.ingsw.am48.model.enums.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameDataLoaderTest {

    private BoardDTO boardDTO;

    @BeforeEach
    void setUp() {
        boardDTO = new GameDataLoader().loadData();
    }

    // ==================== loadData: file and structure ====================

    @Test
    @DisplayName("loadData: should load a non-null BoardDTO from game_data.json")
    void shouldLoadNonNullBoardDTO() {
        assertNotNull(boardDTO);
    }

    @Test
    @DisplayName("loadData: should deserialize a non-empty characters list")
    void shouldDeserializeNonEmptyCharactersList() {
        assertNotNull(boardDTO.characters);
        assertFalse(boardDTO.characters.isEmpty());
    }

    @Test
    @DisplayName("loadData: should deserialize a non-empty events list")
    void shouldDeserializeNonEmptyEventsList() {
        assertNotNull(boardDTO.events);
        assertFalse(boardDTO.events.isEmpty());
    }

    @Test
    @DisplayName("loadData: should deserialize a non-empty buildings list")
    void shouldDeserializeNonEmptyBuildingsList() {
        assertNotNull(boardDTO.buildings);
        assertFalse(boardDTO.buildings.isEmpty());
    }

    @Test
    @DisplayName("loadData: should deserialize a non-empty offerCards list")
    void shouldDeserializeNonEmptyOfferCardsList() {
        assertNotNull(boardDTO.offerCards);
        assertFalse(boardDTO.offerCards.isEmpty());
    }

    @Test
    @DisplayName("loadData: should deserialize a non-null buildingSetup map")
    void shouldDeserializeNonNullBuildingSetupMap() {
        assertNotNull(boardDTO.buildingSetup);
        assertFalse(boardDTO.buildingSetup.isEmpty());
    }

    // ==================== loadData: characters ====================

    @Test
    @DisplayName("loadData: should deserialize the correct total number of character cards")
    void shouldDeserializeCorrectNumberOfCharacters() {
        assertEquals(84, boardDTO.characters.size());
    }

    @Test
    @DisplayName("loadData: should correctly deserialize first character card fields")
    void shouldDeserializeFirstCharacterCardCorrectly() {
        CardDTO first = boardDTO.characters.get(0);
        assertEquals("ART-01", first.id);
        assertEquals("FIRST", first.era);
        assertEquals(2, first.minPlayers);
        assertEquals("ARTIST", first.character);
        assertNull(first.strategy);
    }

    @Test
    @DisplayName("loadData: should correctly deserialize a character card with a strategy")
    void shouldDeserializeCharacterCardWithStrategyCorrectly() {
        CardDTO bui01 = boardDTO.characters.stream()
                .filter(c -> c.id.equals("BUI-01"))
                .findFirst()
                .orElseThrow();
        assertNotNull(bui01.strategy);
        assertEquals("BuilderStrategy", bui01.strategy.effect);
        // nota: BuilderStrategy nei character non ha notificator nel JSON aggiornato
    }

    @Test
    @DisplayName("loadData: should deserialize all character eras as valid non-null strings")
    void shouldDeserializeAllCharacterErasAsValidStrings() {
        boardDTO.characters.forEach(c -> {
            assertNotNull(c.era);
            assertTrue(c.era.equals("FIRST") || c.era.equals("SECOND") || c.era.equals("THIRD"),
                    "Unexpected era value: " + c.era);
        });
    }

    @Test
    @DisplayName("loadData: should deserialize all character ids as non-null")
    void shouldDeserializeAllCharacterIdsAsNonNull() {
        boardDTO.characters.forEach(c ->
                assertNotNull(c.id, "Character id should not be null"));
    }

    // ==================== loadData: events ====================

    @Test
    @DisplayName("loadData: should deserialize the correct total number of event cards")
    void shouldDeserializeCorrectNumberOfEvents() {
        assertEquals(12, boardDTO.events.size());
    }

    @Test
    @DisplayName("loadData: should deserialize all event cards with a non-null strategy")
    void shouldDeserializeAllEventsWithNonNullStrategy() {
        boardDTO.events.forEach(e ->
                assertNotNull(e.strategy, "Event " + e.id + " should have a non-null strategy"));
    }

    @Test
    @DisplayName("loadData: should deserialize all event cards with a non-null eventType")
    void shouldDeserializeAllEventsWithNonNullEventType() {
        boardDTO.events.forEach(e ->
                assertNotNull(e.eventType, "Event " + e.id + " should have a non-null eventType"));
    }

    @Test
    @DisplayName("loadData: should deserialize last two events as THIRD era SHAMAN and PICKER type")
    void shouldDeserializeLastTwoEventsCorrectly() {
        int size = boardDTO.events.size();
        assertEquals("THIRD", boardDTO.events.get(size - 2).era);
        assertEquals("THIRD", boardDTO.events.get(size - 1).era);
        assertEquals("SHAMAN_EVENT", boardDTO.events.get(size - 2).eventType);
        assertEquals("PICKER_EVENT", boardDTO.events.get(size - 1).eventType);
    }

    // ==================== loadData: buildings ====================

    @Test
    @DisplayName("loadData: should deserialize the correct total number of building cards")
    void shouldDeserializeCorrectNumberOfBuildings() {
        assertEquals(21, boardDTO.buildings.size());
    }

    @Test
    @DisplayName("loadData: should correctly deserialize first building card fields")
    void shouldDeserializeFirstBuildingCardCorrectly() {
        CardDTO bld01 = boardDTO.buildings.stream()
                .filter(b -> b.id.equals("BLD-01"))
                .findFirst()
                .orElseThrow();
        assertEquals("FIRST", bld01.era);
        assertEquals(3, bld01.foodCost);
        assertEquals(3, bld01.prestigePoints);
        assertNotNull(bld01.strategy);
        assertEquals("ExtraFoodOnFoodStrategy", bld01.strategy.effect);
    }

    @Test
    @DisplayName("loadData: should deserialize building BLD-20 with null strategy")
    void shouldDeserializeBuildingWithNullStrategy() {
        CardDTO bld20 = boardDTO.buildings.stream()
                .filter(b -> b.id.equals("BLD-20"))
                .findFirst()
                .orElseThrow();
        assertNull(bld20.strategy);
        assertEquals(10, bld20.foodCost);
        assertEquals(25, bld20.prestigePoints);
    }

    @Test
    @DisplayName("loadData: should deserialize building BLD-21 with ExtraPickStrategy")
    void shouldDeserializeBld21WithExtraPickStrategy() {
        CardDTO bld21 = boardDTO.buildings.stream()
                .filter(b -> b.id.equals("BLD-21"))
                .findFirst()
                .orElseThrow();
        assertNotNull(bld21.strategy);
        assertEquals("ExtraPickStrategy", bld21.strategy.effect);
        assertEquals("OnPick", bld21.strategy.notificator);
    }

    @Test
    @DisplayName("loadData: should deserialize all building ids as non-null")
    void shouldDeserializeAllBuildingIdsAsNonNull() {
        boardDTO.buildings.forEach(b ->
                assertNotNull(b.id, "Building id should not be null"));
    }

    // ==================== loadData: offerCards ====================

    @Test
    @DisplayName("loadData: should deserialize the correct total number of offer cards")
    void shouldDeserializeCorrectNumberOfOfferCards() {
        assertEquals(7, boardDTO.offerCards.size());
    }

    @Test
    @DisplayName("loadData: should correctly deserialize offer card A with foodBonus and numUp/numDown")
    void shouldDeserializeOfferCardACorrectly() {
        OfferCardDTO cardA = boardDTO.offerCards.stream()
                .filter(o -> o.id == 'A')
                .findFirst()
                .orElseThrow();
        assertEquals(5, cardA.minPlayers);
        assertEquals(3, cardA.foodBonus);
        assertEquals(0, cardA.numUp);
        assertEquals(0, cardA.numDown);
    }

    @Test
    @DisplayName("loadData: should correctly deserialize offer card G with numUp=2 and numDown=1")
    void shouldDeserializeOfferCardGCorrectly() {
        OfferCardDTO cardG = boardDTO.offerCards.stream()
                .filter(o -> o.id == 'G')
                .findFirst()
                .orElseThrow();
        assertEquals(4, cardG.minPlayers);
        assertEquals(2, cardG.numUp);
        assertEquals(1, cardG.numDown);
        assertEquals(0, cardG.foodBonus);
    }

    @Test
    @DisplayName("loadData: should deserialize all offer cards with non-negative numUp and numDown")
    void shouldDeserializeAllOfferCardsWithValidPickCounts() {
        boardDTO.offerCards.forEach(o -> {
            assertTrue(o.numUp >= 0, "numUp should be non-negative for card " + o.id);
            assertTrue(o.numDown >= 0, "numDown should be non-negative for card " + o.id);
        });
    }

    // ==================== loadData: offerTurnCard ====================

    @Test
    @DisplayName("loadData: should deserialize all four offerTurnCard entries from JSON")
    void shouldDeserializeAllFourOfferTurnCardEntries() {
        assertNotNull(boardDTO.offerTurnCard);
        assertEquals(4, boardDTO.offerTurnCard.size());
    }

    @Test
    @DisplayName("loadData: should deserialize one offerTurnCard entry for each valid player count")
    void shouldDeserializeOneOfferTurnCardPerPlayerCount() {
        List<Integer> playerCounts = boardDTO.offerTurnCard.stream()
                .map(o -> o.numPlayers)
                .toList();
        assertTrue(playerCounts.containsAll(List.of(2, 3, 4, 5)));
    }

    @Test
    @DisplayName("loadData: should correctly deserialize offerTurnCard for 2 players")
    void shouldDeserializeOfferTurnCardForTwoPlayersCorrectly() {
        OfferTurnCardDTO dto = boardDTO.offerTurnCard.stream()
                .filter(o -> o.numPlayers == 2)
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(1, 0, 1), dto.foodRewards);
        assertEquals(2, dto.ppPenalty);
    }

    @Test
    @DisplayName("loadData: should correctly deserialize offerTurnCard for 5 players")
    void shouldDeserializeOfferTurnCardForFivePlayersCorrectly() {
        OfferTurnCardDTO dto = boardDTO.offerTurnCard.stream()
                .filter(o -> o.numPlayers == 5)
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(3, 1, 1), dto.foodRewards);
        assertEquals(2, dto.ppPenalty);
    }

    // ==================== loadData: buildingSetup ====================

    @Test
    @DisplayName("loadData: should deserialize buildingSetup for all four player counts")
    void shouldDeserializeBuildingSetupForAllPlayerCounts() {
        assertTrue(boardDTO.buildingSetup.containsKey(2));
        assertTrue(boardDTO.buildingSetup.containsKey(3));
        assertTrue(boardDTO.buildingSetup.containsKey(4));
        assertTrue(boardDTO.buildingSetup.containsKey(5));
    }

    @Test
    @DisplayName("loadData: should correctly deserialize buildingSetup for 2 players")
    void shouldDeserializeBuildingSetupForTwoPlayersCorrectly() {
        Map<Era, Integer> setup = boardDTO.buildingSetup.get(2);
        assertEquals(1, setup.get(Era.FIRST));
        assertEquals(2, setup.get(Era.SECOND));
        assertEquals(3, setup.get(Era.THIRD));
    }

    @Test
    @DisplayName("loadData: should correctly deserialize buildingSetup for 5 players")
    void shouldDeserializeBuildingSetupForFivePlayersCorrectly() {
        Map<Era, Integer> setup = boardDTO.buildingSetup.get(5);
        assertEquals(2, setup.get(Era.FIRST));
        assertEquals(3, setup.get(Era.SECOND));
        assertEquals(5, setup.get(Era.THIRD));
    }
}