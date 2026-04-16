package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.BoardSnapshot;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardTest {

    private OfferCardTrack track;
    private OfferTurnCard turnOrder;
    private Deck<Card> tribeDeck;
    private Deck<BuildingCard> buildingDeck;
    private Showed<Card> tribeShowed;
    private Showed<BuildingCard> buildingShowed;
    private List<Integer> buildingsPerEra;
    private Board board;
    private NotificatorCenter nc;
    private PlayerContext playerContext;

    private Player playerA;
    private Player playerB;
    private Card cardA;
    private BuildingCard buildingCardA;

    private static final int NUM_PLAYERS = 2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        track          = mock(OfferCardTrack.class);
        turnOrder      = mock(OfferTurnCard.class);
        tribeDeck      = mock(Deck.class);
        buildingDeck   = mock(Deck.class);
        tribeShowed    = mock(Showed.class);
        buildingShowed = mock(Showed.class);
        nc             = mock(NotificatorCenter.class);
        playerContext  = mock(PlayerContext.class);
        buildingsPerEra = List.of(1, 2, 3);

        playerA       = mock(Player.class);
        playerB       = mock(Player.class);
        cardA         = mock(Card.class);
        buildingCardA = mock(BuildingCard.class);

        board = new Board(track, turnOrder, tribeDeck, buildingDeck,
                tribeShowed, buildingShowed, buildingsPerEra, NUM_PLAYERS);
    }

    // ==================== placeTotem ====================

    @Test
    @DisplayName("placeTotem: should delegate to track when player matches expected next totem")
    void shouldDelegatePlaceTotemToTrackWhenPlayerIsExpected() {
        when(turnOrder.getNextTotem()).thenReturn(playerA);

        board.placeTotem(playerA, 'B');

        verify(track, times(1)).placeTotem(playerA, 'B');
        verify(turnOrder, times(1)).removeNextTotem();
    }

    @Test
    @DisplayName("placeTotem: should throw IllegalStateException when player is not the expected next totem")
    void shouldThrowWhenPlayerIsNotExpectedNextTotem() {
        when(turnOrder.getNextTotem()).thenReturn(playerB);

        assertThrows(IllegalStateException.class, () -> board.placeTotem(playerA, 'B'));
        verify(track, never()).placeTotem(any(), anyChar());
        verify(turnOrder, never()).removeNextTotem();
    }

    @Test
    @DisplayName("placeTotem: should throw IllegalStateException when order is empty")
    void shouldThrowWhenNextTotemIsNull() {
        when(turnOrder.getNextTotem()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> board.placeTotem(playerA, 'B'));
        verify(track, never()).placeTotem(any(), anyChar());
    }

    @Test
    @DisplayName("placeTotem: should not interact with decks or showed")
    void shouldNotInteractWithDecksOrShowedOnPlaceTotem() {
        when(turnOrder.getNextTotem()).thenReturn(playerA);

        board.placeTotem(playerA, 'B');

        verifyNoInteractions(tribeDeck, buildingDeck, tribeShowed, buildingShowed);
    }

    // ==================== returnTotem ====================

    @Test
    @DisplayName("returnTotem: should delegate to turnOrder.returnTotem with correct player")
    void shouldDelegateReturnTotemToTurnOrder() {
        board.returnTotem(playerA);
        verify(turnOrder, times(1)).returnTotem(playerA);
    }

    @Test
    @DisplayName("returnTotem: should not interact with any other dependency")
    void shouldNotInteractWithOtherDependenciesOnReturnTotem() {
        board.returnTotem(playerA);
        verifyNoInteractions(track, tribeDeck, buildingDeck, tribeShowed, buildingShowed);
    }

    // ==================== getPickOrder ====================

    @Test
    @DisplayName("getPickOrder: should delegate to track and return its result")
    void shouldGetCorrectPickOrderFromTrack() {
        List<Player> expected = List.of(playerA, playerB);
        when(track.getPickOrder()).thenReturn(expected);

        List<Player> result = board.getPickOrder();

        assertEquals(expected, result);
        verify(track, times(1)).getPickOrder();
    }

    @Test
    @DisplayName("getPickOrder: should not interact with any other dependency")
    void shouldOnlyInteractWithTrackForPickOrder() {
        when(track.getPickOrder()).thenReturn(List.of());
        board.getPickOrder();
        verifyNoInteractions(turnOrder, tribeDeck, buildingDeck, tribeShowed, buildingShowed);
    }

    // ==================== takeCard ====================

    @Test
    @DisplayName("takeCard: should return card from tribeShowed when found there")
    void shouldReturnCardFromTribeShowed() {
        when(tribeShowed.takeCard(playerA, "ART-01")).thenReturn(Optional.of(cardA));

        Card result = board.takeCard(playerA, "ART-01");

        assertEquals(cardA, result);
        verify(tribeShowed, times(1)).takeCard(playerA, "ART-01");
    }

    @Test
    @DisplayName("takeCard: should fall back to buildingShowed when card not in tribeShowed")
    void shouldFindInBuildingShowedIfNotInTribeShowed() {
        when(tribeShowed.takeCard(playerA, "BLD-01")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "BLD-01")).thenReturn(Optional.of(buildingCardA));

        Card result = board.takeCard(playerA, "BLD-01");

        assertEquals(buildingCardA, result);
        verify(tribeShowed, times(1)).takeCard(playerA, "BLD-01");
        verify(buildingShowed, times(1)).takeCard(playerA, "BLD-01");
    }

    @Test
    @DisplayName("takeCard: should throw IllegalArgumentException when card not found in either showed")
    void shouldThrowWhenCardNotFound() {
        when(tribeShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> board.takeCard(playerA, "unknown"));
    }

    @Test
    @DisplayName("takeCard: exception message should contain the card id")
    void shouldIncludeCardIdInExceptionMessage() {
        when(tribeShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> board.takeCard(playerA, "unknown"));
        assertTrue(ex.getMessage().contains("unknown"));
    }

    // ==================== isCardTop ====================

    @Test
    @DisplayName("isCardTop: should return true when card is on top row of tribeShowed")
    void shouldReturnTrueForTribeShowedIfTop() {
        when(tribeShowed.isTop("ART-01")).thenReturn(true);
        assertTrue(board.isCardTop("ART-01"));
    }

    @Test
    @DisplayName("isCardTop: should return true when card is on top row of buildingShowed")
    void shouldReturnTrueForBuildingShowedIfTop() {
        when(tribeShowed.isTop("BLD-01")).thenReturn(false);
        when(buildingShowed.isTop("BLD-01")).thenReturn(true);
        assertTrue(board.isCardTop("BLD-01"));
    }

    @Test
    @DisplayName("isCardTop: should return false when card is not on top row of either showed")
    void shouldReturnFalseWhenNotFoundInTop() {
        when(tribeShowed.isTop("unknown")).thenReturn(false);
        when(buildingShowed.isTop("unknown")).thenReturn(false);
        assertFalse(board.isCardTop("unknown"));
    }

    // ==================== isCardDown ====================

    @Test
    @DisplayName("isCardDown: should return true when card is on lower row of tribeShowed")
    void shouldReturnTrueForTribeShowedIfDown() {
        when(tribeShowed.isDown("ART-01")).thenReturn(true);
        assertTrue(board.isCardDown("ART-01"));
    }

    @Test
    @DisplayName("isCardDown: should return true when card is on lower row of buildingShowed")
    void shouldReturnTrueForBuildingShowedIfDown() {
        when(tribeShowed.isDown("BLD-01")).thenReturn(false);
        when(buildingShowed.isDown("BLD-01")).thenReturn(true);
        assertTrue(board.isCardDown("BLD-01"));
    }

    @Test
    @DisplayName("isCardDown: should return false when card is not on lower row of either showed")
    void shouldReturnFalseWhenNotFoundInBottom() {
        when(tribeShowed.isDown("unknown")).thenReturn(false);
        when(buildingShowed.isDown("unknown")).thenReturn(false);
        assertFalse(board.isCardDown("unknown"));
    }

    // ==================== findTrackPosition ====================

    @Test
    @DisplayName("findTrackPosition: should delegate to track and return its result")
    void shouldDelegateFindTrackPositionToTrack() {
        OfferCard offerCard = mock(OfferCard.class);
        when(track.findTrackPosition(playerA)).thenReturn(offerCard);

        OfferCard result = board.findTrackPosition(playerA);

        assertEquals(offerCard, result);
        verify(track, times(1)).findTrackPosition(playerA);
    }

    @Test
    @DisplayName("findTrackPosition: should propagate exception from track when player not found")
    void shouldPropagateExceptionWhenPlayerNotOnTrack() {
        when(track.findTrackPosition(playerA))
                .thenThrow(new IllegalArgumentException("player not found"));

        assertThrows(IllegalArgumentException.class, () -> board.findTrackPosition(playerA));
    }

    // ==================== setupBoard ====================

    @Test
    @DisplayName("setupBoard: should draw numPlayers+1 tribe cards and add them to lower row")
    void shouldDisplayCorrectNumberOfTribeCardsOnLowerRow() {
        List<Card> lowerCards = List.of(mock(Card.class), mock(Card.class), mock(Card.class));
        when(tribeDeck.drawCards(NUM_PLAYERS + 1)).thenReturn(lowerCards);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        board.setupBoard(List.of(playerA, playerB));

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 1);
        verify(tribeShowed, times(1)).addLowerCards(lowerCards);
    }

    @Test
    @DisplayName("setupBoard: should draw numPlayers+4 tribe cards and add them to upper row")
    void shouldDisplayCorrectNumberOfTribeCardsOnUpperRow() {
        List<Card> upperCards = List.of(mock(Card.class), mock(Card.class),
                mock(Card.class), mock(Card.class), mock(Card.class), mock(Card.class));
        when(tribeDeck.drawCards(NUM_PLAYERS + 1)).thenReturn(List.of());
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(upperCards);
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        board.setupBoard(List.of(playerA, playerB));

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 4);
        verify(tribeShowed, times(1)).addUpperCards(upperCards);
    }

    @Test
    @DisplayName("setupBoard: should draw correct number of building cards for Era.FIRST")
    void shouldDisplayCorrectBuildingCardsForFirstEra() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        List<BuildingCard> buildings = List.of(mock(BuildingCard.class));
        when(buildingDeck.drawCards(1)).thenReturn(buildings);

        board.setupBoard(List.of(playerA, playerB));

        verify(buildingDeck, times(1)).drawCards(1);
        verify(buildingShowed, times(1)).addUpperCards(buildings);
    }

    @Test
    @DisplayName("setupBoard: should delegate turn order setup to turnOrder")
    void shouldDelegateTurnOrderSetupToOfferTurnCard() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        List<Player> players = List.of(playerA, playerB);
        board.setupBoard(players);

        verify(turnOrder, times(1)).setupOrder(players);
    }

    // ==================== endTurn ====================

    @Test
    @DisplayName("endTurn: should clear bottom row and shift tribe showed")
    void shouldClearAndShiftTribeShowed() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn(nc, playerContext);

        verify(tribeShowed, times(1)).clearBottom();
        verify(tribeShowed, times(1)).shiftRow();
    }

    @Test
    @DisplayName("endTurn: should call registerBottom on tribeShowed with nc and playerContext")
    void shouldCallRegisterBottomOnTribeShowed() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn(nc, playerContext);

        verify(tribeShowed, times(1)).registerBottom(nc, playerContext);
    }

    @Test
    @DisplayName("endTurn: should draw and display new tribe cards after shifting")
    void shouldDisplayNewTribeCardsAfterShift() {
        List<Card> newCards = List.of(mock(Card.class));
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(newCards);
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn(nc, playerContext);

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 4);
        verify(tribeShowed, times(1)).addUpperCards(newCards);
    }

    @Test
    @DisplayName("endTurn: should not change era when diffLastEras returns false")
    void shouldNotChangeEraWhenDiffLastErasIsFalse() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn(nc, playerContext);

        verify(buildingShowed, never()).clearBottom();
        verify(buildingShowed, never()).shiftRow();
        verify(buildingDeck, never()).drawCards(anyInt());
    }

    @Test
    @DisplayName("endTurn: should trigger era change when diffLastEras returns true")
    void shouldChangeEraWhenDiffLastErasIsTrue() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(true);

        board.endTurn(nc, playerContext);

        verify(buildingShowed, times(1)).clearBottom();
        verify(buildingShowed, times(1)).shiftRow();
        verify(buildingDeck, times(1)).drawCards(anyInt());
    }

    @Test
    @DisplayName("endTurn: should draw building cards for the next era on era change")
    void shouldDrawBuildingCardsForNextEraOnEraChange() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(true);
        List<BuildingCard> newBuildings = List.of(mock(BuildingCard.class), mock(BuildingCard.class));
        // Era advances FIRST → SECOND, index=1, buildingsPerEra.get(1)=2
        when(buildingDeck.drawCards(2)).thenReturn(newBuildings);

        board.endTurn(nc, playerContext);

        verify(buildingDeck, times(1)).drawCards(2);
        verify(buildingShowed, times(1)).addUpperCards(newBuildings);
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a non-null snapshot")
    void shouldReturnNonNullSnapshot() {
        when(tribeShowed.getUpperList()).thenReturn(List.of());
        when(tribeShowed.getLowerList()).thenReturn(List.of());
        when(buildingShowed.getUpperList()).thenReturn(List.of());
        when(buildingShowed.getLowerList()).thenReturn(List.of());
        when(tribeDeck.getRemainingCardIds()).thenReturn(List.of());
        when(buildingDeck.getRemainingCardIds()).thenReturn(List.of());
        when(track.toSnapshot()).thenReturn(mock(OfferTrackSnapshot.class));
        when(turnOrder.toSnapshot()).thenReturn(mock(OfferTurnCardSnapshot.class));

        assertNotNull(board.toSnapshot());
    }

    @Test
    @DisplayName("toSnapshot: should delegate to all dependencies to build the snapshot")
    void shouldDelegateToAllDependenciesToBuildSnapshot() {
        Card upperTribe        = mock(Card.class);
        Card lowerTribe        = mock(Card.class);
        BuildingCard upperBuilding = mock(BuildingCard.class);
        BuildingCard lowerBuilding = mock(BuildingCard.class);

        when(upperTribe.getCardId()).thenReturn("ART-01");
        when(lowerTribe.getCardId()).thenReturn("ART-02");
        when(upperBuilding.getCardId()).thenReturn("BLD-01");
        when(lowerBuilding.getCardId()).thenReturn("BLD-02");

        when(tribeShowed.getUpperList()).thenReturn(List.of(upperTribe));
        when(tribeShowed.getLowerList()).thenReturn(List.of(lowerTribe));
        when(buildingShowed.getUpperList()).thenReturn(List.of(upperBuilding));
        when(buildingShowed.getLowerList()).thenReturn(List.of(lowerBuilding));
        when(tribeDeck.getRemainingCardIds()).thenReturn(List.of("ART-03"));
        when(buildingDeck.getRemainingCardIds()).thenReturn(List.of("BLD-03"));
        when(track.toSnapshot()).thenReturn(mock(OfferTrackSnapshot.class));
        when(turnOrder.toSnapshot()).thenReturn(mock(OfferTurnCardSnapshot.class));

        BoardSnapshot snapshot = board.toSnapshot();

        assertEquals(List.of("ART-01"), snapshot.getUpperRowCardIds());
        assertEquals(List.of("ART-02"), snapshot.getLowerRowCardIds());
        assertEquals(List.of("BLD-01"), snapshot.getBuildingUpperRowCardIds());
        assertEquals(List.of("BLD-02"), snapshot.getBuildingLowerRowCardIds());
        assertEquals(List.of("ART-03"), snapshot.getTribeDeckRemainingIds());
        assertEquals(List.of("BLD-03"), snapshot.getBuildingDeckRemainingIds());
        verify(track, times(1)).toSnapshot();
        verify(turnOrder, times(1)).toSnapshot();
    }
}