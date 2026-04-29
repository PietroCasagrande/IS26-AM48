package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfferCardTrackTest {

    private Player playerA;
    private Player playerB;
    private Player playerC;
    private Player playerD;
    private Player playerE;
    private OfferCard offerCardA;
    private OfferCard offerCardB;
    private OfferCard offerCardC;
    private OfferCard offerCardD;
    private OfferCard offerCardE;
    private OfferCard offerCardF;
    private OfferCard offerCardG;
    private OfferCardTrack offerCardTrack;

    @BeforeEach
    void setUp() {
        playerA = mock(Player.class);
        playerB = mock(Player.class);
        playerC = mock(Player.class);
        playerD = mock(Player.class);
        playerE = mock(Player.class);

        offerCardA = mock(OfferCard.class);
        offerCardB = mock(OfferCard.class);
        offerCardC = mock(OfferCard.class);
        offerCardD = mock(OfferCard.class);
        offerCardE = mock(OfferCard.class);
        offerCardF = mock(OfferCard.class);
        offerCardG = mock(OfferCard.class);

        Map<Character, OfferCard> trackMap = new TreeMap<>();
        trackMap.put('A', offerCardA);
        trackMap.put('B', offerCardB);
        trackMap.put('C', offerCardC);
        trackMap.put('D', offerCardD);
        trackMap.put('E', offerCardE);
        trackMap.put('F', offerCardF);
        trackMap.put('G', offerCardG);

        offerCardTrack = new OfferCardTrack(trackMap);
    }

    // ==================== placeTotem ====================

    @Test
    @DisplayName("placeTotem: should delegate to the correct OfferCard for a valid letter")
    void shouldPlaceTotemOnCorrectOfferCard() {
        offerCardTrack.placeTotem(playerA, 'A');
        verify(offerCardA, times(1)).placeTotem(playerA);
    }

    @Test
    @DisplayName("placeTotem: should throw IllegalArgumentException for an invalid letter")
    void shouldThrowForInvalidLetter() {
        assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.placeTotem(playerA, 'Z'));
    }

    @Test
    @DisplayName("placeTotem: should not interact with other OfferCards when placing on one")
    void shouldNotTouchOtherOfferCardsWhenPlacingTotem() {
        offerCardTrack.placeTotem(playerA, 'B');
        verify(offerCardA, never()).placeTotem(any());
        verify(offerCardC, never()).placeTotem(any());
    }

    @Test
    @DisplayName("placeTotem: exception message should contain the expected text")
    void shouldDisplayCorrectExceptionMessageForInvalidLetter() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.placeTotem(playerA, 'X'));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot place totem: invalid letter id"));
    }

    // ==================== getPickOrder ====================

    @Test
    @DisplayName("getPickOrder: should return players in TreeMap (alphabetical) order skipping empty slots")
    void shouldReturnPlayersInTrackOrder() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.of(playerE));
        when(offerCardF.getTotem()).thenReturn(Optional.of(playerC));
        when(offerCardG.getTotem()).thenReturn(Optional.of(playerD));

        List<Player> order = offerCardTrack.getPickOrder();

        assertEquals(5, order.size());
        assertEquals(playerB, order.get(0));
        assertEquals(playerA, order.get(1));
        assertEquals(playerE, order.get(2));
        assertEquals(playerC, order.get(3));
        assertEquals(playerD, order.get(4));
    }

    @Test
    @DisplayName("getPickOrder: should return empty list when no totems are placed")
    void shouldReturnEmptyListWhenNoTotemsPlaced() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertTrue(offerCardTrack.getPickOrder().isEmpty());
    }

    @Test
    @DisplayName("getPickOrder: should skip cards with no totem")
    void shouldSkipEmptySlotsInPickOrder() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        List<Player> order = offerCardTrack.getPickOrder();
        assertEquals(1, order.size());
        assertEquals(playerA, order.get(0));
    }

    @Test
    @DisplayName("getPickOrder: should return all players when all slots are occupied")
    void shouldReturnAllPlayersWhenAllSlotsOccupied() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardC.getTotem()).thenReturn(Optional.of(playerC));
        when(offerCardD.getTotem()).thenReturn(Optional.of(playerD));
        when(offerCardE.getTotem()).thenReturn(Optional.of(playerE));
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        List<Player> order = offerCardTrack.getPickOrder();
        assertEquals(List.of(playerA, playerB, playerC, playerD, playerE), order);
    }

    // ==================== findTrackPosition ====================

    @Test
    @DisplayName("findTrackPosition: should return the correct OfferCard where the player totem is placed")
    void shouldReturnCorrectOfferCardForPlayer() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertEquals(offerCardA, offerCardTrack.findTrackPosition(playerA));
    }

    @Test
    @DisplayName("findTrackPosition: should throw IllegalArgumentException when player has no totem on track")
    void shouldThrowWhenPlayerHasNoTotemOnTrack() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.findTrackPosition(playerA));
    }

    @Test
    @DisplayName("findTrackPosition: should throw when player's totem is not on track but another player's is")
    void shouldThrowWhenOnlyOtherPlayerIsOnTrack() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.findTrackPosition(playerA));
    }

    @Test
    @DisplayName("findTrackPosition: should return correct card when player is in the middle of the track")
    void shouldFindPlayerTotemInMiddleOfTrack() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertEquals(offerCardB, offerCardTrack.findTrackPosition(playerA));
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a non-null snapshot")
    void shouldReturnNonNullSnapshot() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertNotNull(offerCardTrack.toSnapshot());
    }

    @Test
    @DisplayName("toSnapshot: should produce an empty totemPositions map when no totems are placed")
    void shouldProduceEmptyTotemPositionsWhenNoTotemsPlaced() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        OfferTrackSnapshot snapshot = offerCardTrack.toSnapshot();
        assertTrue(snapshot.getTotemPositions().isEmpty());
    }

    @Test
    @DisplayName("toSnapshot: should include totem entry for each occupied slot")
    void shouldIncludeTotemEntryForEachOccupiedSlot() {
        // player on A, nickname "Alice"
        when(playerA.getNickname()).thenReturn("Alice");
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        OfferTrackSnapshot snapshot = offerCardTrack.toSnapshot();

        assertEquals(1, snapshot.getTotemPositions().size());
        assertEquals("Alice", snapshot.getTotemPositions().get('A'));
    }

    @Test
    @DisplayName("toSnapshot: should map each occupied slot to the correct player nickname")
    void shouldMapEachOccupiedSlotToCorrectTotemName() {
        when(playerA.getNickname()).thenReturn("Alice");
        when(playerB.getNickname()).thenReturn("Bob");
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        OfferTrackSnapshot snapshot = offerCardTrack.toSnapshot();

        assertEquals(2, snapshot.getTotemPositions().size());
        assertEquals("Alice",  snapshot.getTotemPositions().get('A'));
        assertEquals("Bob", snapshot.getTotemPositions().get('B'));
    }
}