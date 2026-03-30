package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
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
    private Player  playerC;
    private Player  playerD;
    private Player  playerE;
    private OfferCard offerCardA;
    private OfferCard offerCardB;
    private OfferCard offerCardC;
    private OfferCard offerCardD;
    private OfferCard offerCardE;
    private OfferCard offerCardF;
    private OfferCard offerCardG;
    private Map<Character, OfferCard> trackMap;
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

        // TreeMap to guarantee order
        trackMap = new TreeMap<>();
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
    @DisplayName("placeTotem: valid letter delegates to the correct OfferCard")
    void shouldPlaceTotemOnOfferCard() {
        offerCardTrack.placeTotem(playerA, 'A');
        verify(offerCardA, times(1)).placeTotem(playerA);
    }

    @Test
    @DisplayName("placeTotem: throws IllegalArgumentException for invalid letter")
    void shouldThrowIllegalArgumentExceptionForInvalidLetter() {
        assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.placeTotem(playerA, 'Z'));
    }

    @Test
    @DisplayName("placeTotem: does not interact with other OfferCards when placing on one")
    void shouldNotTouchOtherOfferCards() {
        offerCardTrack.placeTotem(playerA, 'B');
        verify(offerCardA, never()).placeTotem(any());
        verify(offerCardC, never()).placeTotem(any());
    }

    @Test
    @DisplayName("placeTotem: exception message is correct")
    void shouldDisplayCorrectExceptionMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> offerCardTrack.placeTotem(playerA, 'X'));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot place totem: invalid letter id"));
    }

    // ==================== getActionOrder ====================

    @Test
    @DisplayName("getActionOrder: returns players in TreeMap following track order")
    void shouldReturnCorrectPlayersOrder() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.of(playerE));
        when(offerCardF.getTotem()).thenReturn(Optional.of(playerC));
        when(offerCardG.getTotem()).thenReturn(Optional.of(playerD));

        List<Player> order = offerCardTrack.getActionOrder();

        assertEquals(5, order.size());
        assertEquals(playerB, order.get(0));
        assertEquals(playerA, order.get(1));
        assertEquals(playerE, order.get(2));
        assertEquals(playerC, order.get(3));
        assertEquals(playerD, order.get(4));
    }

    @Test
    @DisplayName("getActionOrder: returns empty list when no totems are placed")
    void shouldReturnEmptyListIfNoTotemsArePlaced() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        List<Player> order = offerCardTrack.getActionOrder();

        assertTrue(order.isEmpty());
    }

    @Test
    @DisplayName("getActionOrder: skips cards with no totem")
    void shouldSkipEmptySlots() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardC.getTotem()).thenReturn(Optional.empty());

        List<Player> order = offerCardTrack.getActionOrder();

        assertEquals(1, order.size());
        assertEquals(playerA, order.get(0));
    }

    @Test
    @DisplayName("getActionOrder: all first cards occupied returns all players in order")
    void shouldReturnAllPlayersInFirstOccupiedCards() {
        Player playerC = mock(Player.class);
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardC.getTotem()).thenReturn(Optional.of(playerC));
        when(offerCardD.getTotem()).thenReturn(Optional.of(playerD));
        when(offerCardE.getTotem()).thenReturn(Optional.of(playerE));

        List<Player> order = offerCardTrack.getActionOrder();

        assertEquals(List.of(playerA, playerB, playerC, playerD, playerE), order);
    }

    // ==================== findTrackPosition ====================

    @Test
    @DisplayName("findTrackPosition: returns correct OfferCard where player totem is placed")
    void shouldReturnCorrectOfferCardForPlayerTotem() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerA));
        OfferCard result = offerCardTrack.findTrackPosition(playerA);
        assertEquals(offerCardA, result);
    }

    @Test
    @DisplayName("findTrackPosition: returns null when player has no totem on track")
    void shouldThrowIllegalArgumentExceptionWhenPlayerHasNoTotemOnTrack() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> offerCardTrack.findTrackPosition(playerA));
    }

    @Test
    @DisplayName("findTrackPosition: does not return card belonging to a different player")
    void shouldNotReturnOtherPlayersTrackPosition() {
        when(offerCardA.getTotem()).thenReturn(Optional.of(playerB));
        when(offerCardB.getTotem()).thenReturn(Optional.empty());
        when(offerCardC.getTotem()).thenReturn(Optional.empty());
        when(offerCardD.getTotem()).thenReturn(Optional.empty());
        when(offerCardE.getTotem()).thenReturn(Optional.empty());
        when(offerCardF.getTotem()).thenReturn(Optional.empty());
        when(offerCardG.getTotem()).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> offerCardTrack.findTrackPosition(playerA));
    }

    @Test
    @DisplayName("findTrackPosition: returns first matching card (no duplicate totems assumed)")
    void findTrackPosition_playerOnMiddleCard_returnsMiddleCard() {
        when(offerCardA.getTotem()).thenReturn(Optional.empty());
        when(offerCardB.getTotem()).thenReturn(Optional.of(playerA));
        when(offerCardC.getTotem()).thenReturn(Optional.empty());

        OfferCard result = offerCardTrack.findTrackPosition(playerA);

        assertEquals(offerCardB, result);
    }
}