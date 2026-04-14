package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OfferCardTest {

    private OfferCard offerCard;
    private Player mockP1;

    @BeforeEach
    void setUp() {
        // letterId='A', numUp=2, numDown=1, foodBonus=3, numPlayers=5
        offerCard = new OfferCard('A', 2, 1, 3, 5);
        mockP1 = mock(Player.class);
    }

    // ==================== placeTotem ====================

    @Test
    @DisplayName("placeTotem: should place totem successfully when slot is empty")
    void shouldPlaceTotemSuccessfullyWhenSlotIsEmpty() {
        assertDoesNotThrow(() -> offerCard.placeTotem(mockP1));
        assertEquals(Optional.of(mockP1), offerCard.getTotem());
    }

    @Test
    @DisplayName("placeTotem: should throw IllegalStateException when slot is already occupied")
    void shouldThrowWhenSlotIsAlreadyOccupied() {
        Player mockP2 = mock(Player.class);
        offerCard.placeTotem(mockP1);
        assertThrows(IllegalStateException.class, () -> offerCard.placeTotem(mockP2));
    }

    // ==================== returnTotem ====================

    @Test
    @DisplayName("returnTotem: should return empty Optional when no totem is present")
    void shouldReturnEmptyWhenNoTotemPresent() {
        assertEquals(Optional.empty(), offerCard.returnTotem());
    }

    @Test
    @DisplayName("returnTotem: should return the placed totem and then clear the slot")
    void shouldReturnTotemAndClearSlot() {
        offerCard.placeTotem(mockP1);
        assertEquals(Optional.of(mockP1), offerCard.returnTotem());
        assertEquals(Optional.empty(), offerCard.returnTotem());
    }

    // ==================== getTotem ====================

    @Test
    @DisplayName("getTotem: should return empty Optional when no totem is present")
    void shouldReturnEmptyGetTotemWhenNoTotemPresent() {
        assertEquals(Optional.empty(), offerCard.getTotem());
    }

    @Test
    @DisplayName("getTotem: should return the totem when one is placed")
    void shouldReturnTotemWhenPresent() {
        offerCard.placeTotem(mockP1);
        assertEquals(Optional.of(mockP1), offerCard.getTotem());
    }

    // ==================== getTotalPicks ====================

    @Test
    @DisplayName("getTotalPicks: should return the sum of numUp and numDown")
    void shouldReturnSumOfNumUpAndNumDown() {
        // numUp=2, numDown=1 → totalPicks=3
        assertEquals(3, offerCard.getTotalPicks());
    }

    @Test
    @DisplayName("getTotalPicks: should return zero when both numUp and numDown are zero")
    void shouldReturnZeroWhenBothPicksAreZero() {
        OfferCard zeroPickCard = new OfferCard('B', 0, 0, 0, 2);
        assertEquals(0, zeroPickCard.getTotalPicks());
    }

    // ==================== getters ====================

    @Test
    @DisplayName("getLetterId: should return the correct letter id")
    void shouldReturnCorrectLetterId() {
        assertEquals('A', offerCard.getLetterId());
    }

    @Test
    @DisplayName("getNumUp: should return the correct number of upper picks")
    void shouldReturnCorrectNumUp() {
        assertEquals(2, offerCard.getNumUp());
    }

    @Test
    @DisplayName("getNumDown: should return the correct number of lower picks")
    void shouldReturnCorrectNumDown() {
        assertEquals(1, offerCard.getNumDown());
    }

    @Test
    @DisplayName("getFoodBonus: should return the correct food bonus")
    void shouldReturnCorrectFoodBonus() {
        assertEquals(3, offerCard.getFoodBonus());
    }

    @Test
    @DisplayName("getNumPlayers: should return the correct number of players")
    void shouldReturnCorrectNumPlayers() {
        assertEquals(5, offerCard.getNumPlayers());
    }
}