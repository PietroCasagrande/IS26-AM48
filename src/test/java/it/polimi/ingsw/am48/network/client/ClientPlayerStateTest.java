package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;
import it.polimi.ingsw.am48.model.snapshot.TribeSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientPlayerStateTest {

    private PlayerSnapshot mockSnapshot;
    private TribeSnapshot mockTribe;

    @BeforeEach
    void setUp() {
        mockSnapshot = mock(PlayerSnapshot.class);
        mockTribe = mock(TribeSnapshot.class);

        when(mockSnapshot.getNickname()).thenReturn("alice");
        when(mockSnapshot.getTotemColor()).thenReturn("RED");
        when(mockSnapshot.getTribe()).thenReturn(mockTribe);
        when(mockTribe.getCurrentFood()).thenReturn(3);
        when(mockTribe.getCurrentPrestigePoints()).thenReturn(5);
        when(mockTribe.getCharacterCardIds()).thenReturn(List.of("c1", "c2"));
        when(mockTribe.getBuildingCardIds()).thenReturn(List.of("b1"));
    }

    // --- fromSnapshot ---

    @Test
    void shouldSetNicknameFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals("alice", state.getNickname());
    }

    @Test
    void shouldSetTotemColorFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals("RED", state.getTotemColor());
    }

    @Test
    void shouldSetFoodFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals(3, state.getFood());
    }

    @Test
    void shouldSetPointsFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals(5, state.getPoints());
    }

    @Test
    void shouldSetCharacterCardsFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("c1", "c2"), state.getCharacterCardIds());
    }

    @Test
    void shouldSetBuildingCardsFromSnapshot() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("b1"), state.getBuildingCardIds());
    }

    @Test
    void shouldReturnDefensiveCopyOfCharacterCards() {
        // la lista restituita non deve essere modificabile
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> state.getCharacterCardIds().add("hacked"));
    }

    @Test
    void shouldReturnDefensiveCopyOfBuildingCards() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> state.getBuildingCardIds().add("hacked"));
    }

    // --- setFood ---

    @Test
    void shouldUpdateFood() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.setFood(10);
        assertEquals(10, state.getFood());
    }

    @Test
    void shouldAllowZeroFood() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.setFood(0);
        assertEquals(0, state.getFood());
    }

    @Test
    void shouldAllowNegativeFood() {
        // il cibo può diventare negativo per penalità
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.setFood(-2);
        assertEquals(-2, state.getFood());
    }

    // --- setPoints ---

    @Test
    void shouldUpdatePoints() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.setPoints(20);
        assertEquals(20, state.getPoints());
    }

    @Test
    void shouldAllowNegativePoints() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.setPoints(-5);
        assertEquals(-5, state.getPoints());
    }

    // --- addCharacterCard ---

    @Test
    void shouldAddCharacterCard() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.addCharacterCard("newCard");
        assertTrue(state.getCharacterCardIds().contains("newCard"));
    }

    @Test
    void shouldPreserveExistingCardsWhenAddingNew() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.addCharacterCard("newCard");
        assertTrue(state.getCharacterCardIds().contains("c1"));
        assertTrue(state.getCharacterCardIds().contains("c2"));
    }

    // --- addBuildingCard ---

    @Test
    void shouldAddBuildingCard() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.addBuildingCard("newBuilding");
        assertTrue(state.getBuildingCardIds().contains("newBuilding"));
    }

    @Test
    void shouldPreserveExistingBuildingsWhenAddingNew() {
        ClientPlayerState state = ClientPlayerState.fromSnapshot(mockSnapshot);
        state.addBuildingCard("newBuilding");
        assertTrue(state.getBuildingCardIds().contains("b1"));
    }
}