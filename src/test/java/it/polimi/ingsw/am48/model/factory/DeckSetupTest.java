package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckSetupTest {

    private CharacterCard charFirst1;
    private CharacterCard charFirst2;
    private CharacterCard charFirst3;
    private CharacterCard charSecond1;
    private CharacterCard charThird1;
    private EventCard eventSecond1;
    private EventCard eventFinalA;
    private EventCard eventFinalB;
    private BuildingCard buildFirst1;
    private BuildingCard buildFirst2;
    private BuildingCard buildSecond1;
    private BuildingCard buildSecond2;
    private BuildingCard buildThird1;
    private BuildingCard buildThird2;

    private Map<Integer, Map<Era, Integer>> buildingSetup;

    @BeforeEach
    void setUp() {
        CardStrategy strategy = mock(CardStrategy.class);

        charFirst1  = new CharacterCard("cf1", Era.FIRST,  strategy, CharacterType.ARTIST, 2);
        charFirst2  = new CharacterCard("cf2", Era.FIRST,  strategy, CharacterType.INVENTOR, 2);
        charFirst3 = new CharacterCard("cf3", Era.FIRST,  strategy, CharacterType.SHAMAN, 2);
        charSecond1 = new CharacterCard("cs1", Era.SECOND, strategy, CharacterType.BUILDER, 2);
        charThird1  = new CharacterCard("ct1", Era.THIRD,  strategy, CharacterType.PICKER, 2);

        eventSecond1 = new EventCard("es1", Era.SECOND, strategy, EventType.HUNTER_EVENT);
        eventFinalA  = new EventCard("et1", Era.THIRD,  strategy, EventType.ARTIST_EVENT);
        eventFinalB  = new EventCard("et2", Era.THIRD,  strategy, EventType.SHAMAN_EVENT);

        buildFirst1  = new BuildingCard("bf1", Era.FIRST,  strategy, 2, 3);
        buildFirst2  = new BuildingCard("bf2", Era.FIRST,  strategy, 2, 3);
        buildSecond1 = new BuildingCard("bs1", Era.SECOND, strategy, 4, 5);
        buildSecond2 = new BuildingCard("bs2", Era.SECOND, strategy, 4, 5);
        buildThird1  = new BuildingCard("bt1", Era.THIRD,  strategy, 6, 7);
        buildThird2  = new BuildingCard("bt2", Era.THIRD,  strategy, 6, 7);

        buildingSetup = new HashMap<>();
        buildingSetup.put(2, Map.of(Era.FIRST, 1, Era.SECOND, 1, Era.THIRD, 1));
        buildingSetup.put(3, Map.of(Era.FIRST, 2, Era.SECOND, 2, Era.THIRD, 2));
    }

    // ==================== Helpers ====================

    private DeckSetup buildDeckSetup() {
        return new DeckSetup(
                new ArrayList<>(List.of(charFirst1, charFirst2, charSecond1, charThird1)),
                new ArrayList<>(List.of(eventSecond1, eventFinalA, eventFinalB)),
                new ArrayList<>(List.of(buildFirst1, buildFirst2, buildSecond1, buildSecond2, buildThird1, buildThird2)),
                buildingSetup
        );
    }

    private long countByEra(List<? extends Card> deck, Era era) {
        return deck.stream().filter(c -> c.getEra().equals(era)).count();
    }

    // ==================== createTribeDeck - validation ====================

    @Test
    @DisplayName("createTribeDeck: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowWhenCreateTribeDeckWithNumPlayersBelowMinimum() {
        DeckSetup setup = buildDeckSetup();
        assertThrows(IllegalArgumentException.class, () -> setup.createTribeDeck(1));
    }

    @Test
    @DisplayName("createTribeDeck: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowWhenCreateTribeDeckWithNumPlayersAboveMaximum() {
        DeckSetup setup = buildDeckSetup();
        assertThrows(IllegalArgumentException.class, () -> setup.createTribeDeck(6));
    }

    @Test
    @DisplayName("createTribeDeck: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayersInCreateTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        assertDoesNotThrow(() -> setup.createTribeDeck(2));
    }

    @Test
    @DisplayName("createTribeDeck: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayersInCreateTribeDeck() {
        CardStrategy s = mock(CardStrategy.class);
        List<CharacterCard> bigCharList = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            bigCharList.add(new CharacterCard("c" + i, Era.FIRST, s, CharacterType.INVENTOR, 2));

        DeckSetup setup = new DeckSetup(
                bigCharList,
                new ArrayList<>(List.of(eventSecond1, eventFinalA, eventFinalB)),
                new ArrayList<>(List.of(buildFirst1, buildFirst2)),
                buildingSetup);
        assertDoesNotThrow(() -> setup.createTribeDeck(5));
    }

    // ==================== createTribeDeck - size and completeness ====================

    @Test
    @DisplayName("createTribeDeck: should contain all characters and events exactly once")
    void shouldContainAllCardsExactlyOnceInTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        assertEquals(7, deck.size()); // 4 chars + 3 events
    }

    @Test
    @DisplayName("createTribeDeck: should not contain duplicate cards")
    void shouldNotContainDuplicatesInTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        long distinctCount = deck.stream().map(Card::getCardId).distinct().count();
        assertEquals(deck.size(), distinctCount);
    }

    @Test
    @DisplayName("createTribeDeck: should contain every character card")
    void shouldContainEveryCharacterCardInTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        assertTrue(deck.contains(charFirst1));
        assertTrue(deck.contains(charFirst2));
        assertTrue(deck.contains(charSecond1));
        assertTrue(deck.contains(charThird1));
    }

    @Test
    @DisplayName("createTribeDeck: should contain every event card")
    void shouldContainEveryEventCardInTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        assertTrue(deck.contains(eventSecond1));
        assertTrue(deck.contains(eventFinalA));
        assertTrue(deck.contains(eventFinalB));
    }

    // ==================== createTribeDeck - top cards ====================

    @Test
    @DisplayName("createTribeDeck: should place exactly numPlayers+1 era-sorted cards at the top")
    void shouldPlaceNumPlayersPlusOneErasortedCardsAtTopOfTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        List<Card> top = deck.subList(0, 2); // numPlayers + 1 = 3
        assertTrue(top.stream().allMatch(c -> c.getEra().equals(Era.FIRST)));
    }

    @Test
    @DisplayName("createTribeDeck: should sort top cards by era ascending")
    void shouldSortTopCardsByEraAscendingInTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(3);
        List<Card> top = deck.subList(0, 4); // numPlayers + 1 = 4

        List<Integer> eraIndices = top.stream()
                .map(c -> c.getEra().getIndex())
                .toList();

        for (int i = 0; i < eraIndices.size() - 1; i++)
            assertTrue(eraIndices.get(i) <= eraIndices.get(i + 1),
                    "Top cards should be sorted by era ascending");
    }

    // ==================== createTribeDeck - final events ====================

    @Test
    @DisplayName("createTribeDeck: should place the two final events at the very end of the deck")
    void shouldPlaceFinalEventsAtEndOfTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        int size = deck.size();
        List<Card> lastTwo = deck.subList(size - 2, size);
        assertTrue(lastTwo.contains(eventFinalA));
        assertTrue(lastTwo.contains(eventFinalB));
    }

    @Test
    @DisplayName("createTribeDeck: should not place final events before the last two positions")
    void shouldNotPlaceFinalEventsBeforeEndOfTribeDeck() {
        DeckSetup setup = buildDeckSetup();
        List<Card> deck = setup.createTribeDeck(2);
        int size = deck.size();
        List<Card> body = deck.subList(0, size - 2);
        assertFalse(body.contains(eventFinalA));
        assertFalse(body.contains(eventFinalB));
    }

    // ==================== createTribeDeck - shuffle ====================

    @Test
    @DisplayName("createTribeDeck: body between top and final events should be shuffled")
    void shouldShuffleBodyOfTribeDeck() {
        CardStrategy s = mock(CardStrategy.class);
        List<CharacterCard> manyChars = new ArrayList<>();
        for (int i = 0; i < 15; i++)
            manyChars.add(new CharacterCard("c" + i, Era.SECOND, s, CharacterType.SHAMAN, 2));

        boolean foundDifference = false;
        for (int i = 0; i < 20; i++) {
            DeckSetup setup1 = new DeckSetup(new ArrayList<>(manyChars),
                    new ArrayList<>(List.of(eventSecond1, eventFinalA, eventFinalB)),
                    new ArrayList<>(List.of(buildFirst1)), buildingSetup);
            DeckSetup setup2 = new DeckSetup(new ArrayList<>(manyChars),
                    new ArrayList<>(List.of(eventSecond1, eventFinalA, eventFinalB)),
                    new ArrayList<>(List.of(buildFirst1)), buildingSetup);

            List<Card> deck1 = setup1.createTribeDeck(2);
            List<Card> deck2 = setup2.createTribeDeck(2);

            List<Card> body1 = deck1.subList(3, deck1.size() - 2);
            List<Card> body2 = deck2.subList(3, deck2.size() - 2);

            if (!body1.equals(body2)) { foundDifference = true; break; }
        }
        assertTrue(foundDifference, "Deck body should be shuffled and differ across independent calls");
    }

    // ==================== createBuildingDeck - validation ====================

    @Test
    @DisplayName("createBuildingDeck: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowWhenCreateBuildingDeckWithNumPlayersBelowMinimum() {
        DeckSetup setup = buildDeckSetup();
        assertThrows(IllegalArgumentException.class, () -> setup.createBuildingDeck(1));
    }

    @Test
    @DisplayName("createBuildingDeck: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowWhenCreateBuildingDeckWithNumPlayersAboveMaximum() {
        DeckSetup setup = buildDeckSetup();
        assertThrows(IllegalArgumentException.class, () -> setup.createBuildingDeck(6));
    }

    @Test
    @DisplayName("createBuildingDeck: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayersInCreateBuildingDeck() {
        DeckSetup setup = buildDeckSetup();
        assertDoesNotThrow(() -> setup.createBuildingDeck(2));
    }

    // ==================== createBuildingDeck - size and era limits ====================

    @Test
    @DisplayName("createBuildingDeck: should respect per-era card limits for 2 players")
    void shouldRespectEraLimitsForTwoPlayersInBuildingDeck() {
        DeckSetup setup = buildDeckSetup();
        List<BuildingCard> deck = setup.createBuildingDeck(2);
        assertEquals(3, deck.size());
        assertEquals(1, countByEra(deck, Era.FIRST));
        assertEquals(1, countByEra(deck, Era.SECOND));
        assertEquals(1, countByEra(deck, Era.THIRD));
    }

    @Test
    @DisplayName("createBuildingDeck: should respect per-era card limits for 3 players")
    void shouldRespectEraLimitsForThreePlayersInBuildingDeck() {
        DeckSetup setup = buildDeckSetup();
        List<BuildingCard> deck = setup.createBuildingDeck(3);
        assertEquals(6, deck.size());
        assertEquals(2, countByEra(deck, Era.FIRST));
        assertEquals(2, countByEra(deck, Era.SECOND));
        assertEquals(2, countByEra(deck, Era.THIRD));
    }

    @Test
    @DisplayName("createBuildingDeck: should not contain duplicate cards")
    void shouldNotContainDuplicatesInBuildingDeck() {
        DeckSetup setup = buildDeckSetup();
        List<BuildingCard> deck = setup.createBuildingDeck(3);
        long distinctCount = deck.stream().map(Card::getCardId).distinct().count();
        assertEquals(deck.size(), distinctCount);
    }

    @Test
    @DisplayName("createBuildingDeck: should contain cards from all three eras")
    void shouldContainCardsFromAllErasInBuildingDeck() {
        DeckSetup setup = buildDeckSetup();
        List<BuildingCard> deck = setup.createBuildingDeck(2);
        Set<Era> presentEras = deck.stream().map(Card::getEra).collect(Collectors.toSet());
        assertTrue(presentEras.containsAll(Set.of(Era.FIRST, Era.SECOND, Era.THIRD)));
    }

    @Test
    @DisplayName("createBuildingDeck: should not exceed available cards per era even if limit is higher")
    void shouldNotExceedAvailableCardsPerEraInBuildingDeck() {
        List<BuildingCard> limitedBuildings = new ArrayList<>(
                List.of(buildFirst1, buildFirst2, buildSecond1, buildSecond2, buildThird1));

        DeckSetup setup = new DeckSetup(
                new ArrayList<>(List.of(charFirst1, charFirst2, charFirst3, charSecond1, charThird1)),
                new ArrayList<>(List.of(eventSecond1, eventFinalA, eventFinalB)),
                limitedBuildings,
                buildingSetup);

        List<BuildingCard> deck = setup.createBuildingDeck(3);
        assertEquals(1, countByEra(deck, Era.THIRD));
        assertEquals(2, countByEra(deck, Era.FIRST));
        assertEquals(2, countByEra(deck, Era.SECOND));
    }
}