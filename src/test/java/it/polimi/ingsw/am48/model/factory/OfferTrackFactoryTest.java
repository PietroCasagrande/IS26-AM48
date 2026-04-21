package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferCardDTO;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.board.OfferCardTrack;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.enums.Totem;
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

    // ==================== createCards - validation ====================

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

    // ==================== createCards - structure ====================

    @Test
    @DisplayName("createCards: should always return a list with exactly one OfferCardTrack")
    void shouldReturnListWithExactlyOneOfferCardTrack() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        List<OfferCardTrack> result = factory.createCards(3);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("createCards: should return a non-null OfferCardTrack")
    void shouldReturnNonNullOfferCardTrack() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        assertNotNull(factory.createCards(2).getFirst());
    }

    @Test
    @DisplayName("createCards: should return one OfferCardTrack even when DTO list is empty")
    void shouldReturnOneTrackEvenForEmptyDtoList() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of());
        List<OfferCardTrack> result = factory.createCards(2);
        assertEquals(1, result.size());
    }

    // ==================== createCards - OfferCard mapping ====================

    @Test
    @DisplayName("createCards: should map letterId 'A' correctly — placeTotem on A should not throw")
    void shouldMapLetterIdACorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        OfferCardTrack track = factory.createCards(2).getFirst();
        Player player = new Player("Alice", Totem.RED);

        assertDoesNotThrow(() -> track.placeTotem(player, 'A'));
    }

    @Test
    @DisplayName("createCards: should map letterId 'B' correctly — placeTotem on B should not throw")
    void shouldMapLetterIdBCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoB));
        OfferCardTrack track = factory.createCards(3).getFirst();
        Player player = new Player("Bob", Totem.BLUE);

        assertDoesNotThrow(() -> track.placeTotem(player, 'B'));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException when placing on an unmapped letter")
    void shouldThrowForUnmappedLetter() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        OfferCardTrack track = factory.createCards(2).getFirst();
        Player player = new Player("Alice", Totem.RED);

        assertThrows(IllegalArgumentException.class, () -> track.placeTotem(player, 'Z'));
    }

    @Test
    @DisplayName("createCards: should map all provided DTOs as slots in the track")
    void shouldMapAllDtosAsTrackSlots() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA, dtoB, dtoC));
        OfferCardTrack track = factory.createCards(3).getFirst();

        Player pA = new Player("Alice",   Totem.RED);
        Player pB = new Player("Bob",     Totem.BLUE);
        Player pC = new Player("Charlie", Totem.WHITE);

        track.placeTotem(pA, 'A');
        track.placeTotem(pB, 'B');
        track.placeTotem(pC, 'C');

        // TreeMap garantisce ordine alfabetico → pickOrder = A, B, C
        List<Player> order = track.getPickOrder();
        assertEquals(List.of(pA, pB, pC), order);
    }

    @Test
    @DisplayName("createCards: should use a TreeMap — pickOrder follows alphabetical slot order")
    void shouldUseTreeMapForAlphabeticalOrder() {
        // Inserisco dtoC prima di dtoA: il TreeMap deve comunque ordinarli A → C
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoC, dtoA));
        OfferCardTrack track = factory.createCards(2).getFirst();

        Player pA = new Player("Alice",   Totem.RED);
        Player pC = new Player("Charlie", Totem.WHITE);

        track.placeTotem(pA, 'A');
        track.placeTotem(pC, 'C');

        List<Player> order = track.getPickOrder();
        assertEquals(pA, order.get(0)); // A prima di C
        assertEquals(pC, order.get(1));
    }

    @Test
    @DisplayName("createCards: should correctly map numUp, numDown and foodBonus via OfferCard getters")
    void shouldMapNumUpNumDownFoodBonusCorrectly() {
        OfferTrackFactory factory = new OfferTrackFactory(List.of(dtoA));
        OfferCardTrack track = factory.createCards(2).getFirst();

        Player player = new Player("Alice", Totem.RED);
        track.placeTotem(player, 'A');

        OfferCard card = track.findTrackPosition(player);
        assertEquals(1, card.getNumUp());
        assertEquals(0, card.getNumDown());
        assertEquals(3, card.getFoodBonus());
        assertEquals(2, card.getNumPlayers());
    }
}