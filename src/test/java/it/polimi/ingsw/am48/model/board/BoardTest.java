package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.player.Tribe;
import it.polimi.ingsw.am48.model.snapshot.BoardSnapshot;
import it.polimi.ingsw.am48.model.snapshot.OfferTrackSnapshot;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardTest {

    private OfferCardTrack track;
    private OfferTurnCard turnOrder;
    private Deck<Card> tribeDeck;
    private Deck<BuildingCard> buildingDeck;
    private List<Integer> buildingsPerEra;
    private Board board;
    private NotificatorCenter nc;
    private PlayerContext playerContext;

    private CardStrategy mockStrategy;
    private Tribe mockTribe;

    private Player playerA;
    private Player playerB;

    // Real player and context used for takeCard tests
    private Player realPlayer;
    private PlayerContext realPlayerContext;

    private static final int NUM_PLAYERS = 2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        track          = mock(OfferCardTrack.class);
        turnOrder      = mock(OfferTurnCard.class);
        tribeDeck      = mock(Deck.class);
        buildingDeck   = mock(Deck.class);
        nc             = mock(NotificatorCenter.class);
        playerContext  = mock(PlayerContext.class);
        buildingsPerEra = List.of(1, 2, 3);

        mockStrategy = mock(CardStrategy.class);
        mockTribe    = mock(Tribe.class);

        playerA = mock(Player.class);
        playerB = mock(Player.class);

        when(playerA.getTribe()).thenReturn(mockTribe);
        when(playerB.getTribe()).thenReturn(mockTribe);
        when(mockTribe.getBuildingDiscount()).thenReturn(0);

        realPlayer = new Player("Alice", Totem.RED);
        realPlayerContext = new PlayerContext();
        realPlayerContext.addPlayer(realPlayer);
        realPlayerContext.setCurrPlayer(realPlayer);

        board = new Board(track, turnOrder, tribeDeck, buildingDeck, buildingsPerEra, NUM_PLAYERS);
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
    @DisplayName("placeTotem: should not interact with decks")
    void shouldNotInteractWithDecksOnPlaceTotem() {
        when(turnOrder.getNextTotem()).thenReturn(playerA);

        board.placeTotem(playerA, 'B');

        verifyNoInteractions(tribeDeck, buildingDeck);
    }

    // ==================== returnTotem ====================

    @Test
    @DisplayName("returnTotem: should delegate to turnOrder.returnTotem with correct player")
    void shouldDelegateReturnTotemToTurnOrder() {
        board.returnTotem(playerA);
        verify(turnOrder, times(1)).returnTotem(playerA);
    }

    @Test
    @DisplayName("returnTotem: should not interact with track or decks")
    void shouldNotInteractWithOtherDependenciesOnReturnTotem() {
        board.returnTotem(playerA);
        verifyNoInteractions(track, tribeDeck, buildingDeck);
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
    @DisplayName("getPickOrder: should not interact with decks or turnOrder")
    void shouldOnlyInteractWithTrackForPickOrder() {
        when(track.getPickOrder()).thenReturn(List.of());
        board.getPickOrder();
        verifyNoInteractions(turnOrder, tribeDeck, buildingDeck);
    }

    // ==================== takeCard ====================

    @Test
    @DisplayName("takeCard: should return card from tribeShowed upper list")
    void shouldReturnCardFromTribeShowedUpperList() {
        CharacterCard card = new CharacterCard("ART-01", Era.FIRST, null, CharacterType.ARTIST, 2);
        board.getTribeShowed().addUpperCards(List.of(card));

        Card result = board.takeCard(realPlayerContext, "ART-01");

        assertEquals(card, result);
        assertTrue(board.getTribeShowed().getUpperList().isEmpty());
    }

    @Test
    @DisplayName("takeCard: should return card from tribeShowed lower list when not in upper")
    void shouldReturnCardFromTribeShowedLowerList() {
        CharacterCard card = new CharacterCard("ART-02", Era.FIRST, null, CharacterType.ARTIST, 2);
        board.getTribeShowed().addLowerCards(List.of(card));

        Card result = board.takeCard(realPlayerContext, "ART-02");

        assertEquals(card, result);
        assertTrue(board.getTribeShowed().getLowerList().isEmpty());
    }

    @Test
    @DisplayName("takeCard: should fall back to buildingShowed when card not in tribeShowed")
    void shouldFindInBuildingShowedIfNotInTribeShowed() {
        realPlayer.updateFood(10);
        BuildingCard buildingCard = new BuildingCard("BLD-01", Era.FIRST, mockStrategy, 3, 5);
        board.getBuildingShowed().addUpperCards(List.of(buildingCard));

        Card result = board.takeCard(realPlayerContext, "BLD-01");

        assertEquals(buildingCard, result);
        assertTrue(board.getBuildingShowed().getUpperList().isEmpty());
    }

    @Test
    @DisplayName("takeCard: should throw IllegalArgumentException when card not found in either showed")
    void shouldThrowWhenCardNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> board.takeCard(realPlayerContext, "unknown"));
    }

    @Test
    @DisplayName("takeCard: exception message should contain the card id")
    void shouldIncludeCardIdInExceptionMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> board.takeCard(realPlayerContext, "unknown"));
        assertTrue(ex.getMessage().contains("unknown"));
    }

    // ==================== isCardTop ====================

    @Test
    @DisplayName("isCardTop: should return true when card is in tribeShowed upper list")
    void shouldReturnTrueForTribeShowedIfTop() {
        board.getTribeShowed().addUpperCards(List.of(
                new CharacterCard("ART-01", Era.FIRST, null, CharacterType.ARTIST, 2)));
        assertTrue(board.isCardTop("ART-01"));
    }

    @Test
    @DisplayName("isCardTop: should return true when card is in buildingShowed upper list")
    void shouldReturnTrueForBuildingShowedIfTop() {
        board.getBuildingShowed().addUpperCards(List.of(
                new BuildingCard("BLD-01", Era.FIRST, null, 3, 5)));
        assertTrue(board.isCardTop("BLD-01"));
    }

    @Test
    @DisplayName("isCardTop: should return false when card is not in upper list of either showed")
    void shouldReturnFalseWhenNotFoundInTop() {
        assertFalse(board.isCardTop("unknown"));
    }

    // ==================== isCardDown ====================

    @Test
    @DisplayName("isCardDown: should return true when card is in tribeShowed lower list")
    void shouldReturnTrueForTribeShowedIfDown() {
        board.getTribeShowed().addLowerCards(List.of(
                new CharacterCard("ART-01", Era.FIRST, null, CharacterType.ARTIST, 2)));
        assertTrue(board.isCardDown("ART-01"));
    }

    @Test
    @DisplayName("isCardDown: should return true when card is in buildingShowed lower list")
    void shouldReturnTrueForBuildingShowedIfDown() {
        board.getBuildingShowed().addLowerCards(List.of(
                new BuildingCard("BLD-01", Era.FIRST, null, 3, 5)));
        assertTrue(board.isCardDown("BLD-01"));
    }

    @Test
    @DisplayName("isCardDown: should return false when card is not in lower list of either showed")
    void shouldReturnFalseWhenNotFoundInBottom() {
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
        CharacterCard c1 = new CharacterCard("c1", Era.FIRST, null, CharacterType.ARTIST, 2);
        CharacterCard c2 = new CharacterCard("c2", Era.FIRST, null, CharacterType.ARTIST, 2);
        CharacterCard c3 = new CharacterCard("c3", Era.FIRST, null, CharacterType.ARTIST, 2);
        List<Card> lowerCards = List.of(c1, c2, c3);

        when(tribeDeck.drawCards(NUM_PLAYERS + 1)).thenReturn(lowerCards);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        board.setupBoard(List.of(playerA, playerB));

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 1);
        assertEquals(lowerCards, board.getTribeShowed().getLowerList());
    }

    @Test
    @DisplayName("setupBoard: should draw numPlayers+4 tribe cards and add them to upper row")
    void shouldDisplayCorrectNumberOfTribeCardsOnUpperRow() {
        List<Card> upperCards = List.of(
                new CharacterCard("u1", Era.FIRST, null, CharacterType.ARTIST, 2),
                new CharacterCard("u2", Era.FIRST, null, CharacterType.ARTIST, 2),
                new CharacterCard("u3", Era.FIRST, null, CharacterType.ARTIST, 2),
                new CharacterCard("u4", Era.FIRST, null, CharacterType.ARTIST, 2),
                new CharacterCard("u5", Era.FIRST, null, CharacterType.ARTIST, 2),
                new CharacterCard("u6", Era.FIRST, null, CharacterType.ARTIST, 2));

        when(tribeDeck.drawCards(NUM_PLAYERS + 1)).thenReturn(List.of());
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(upperCards);
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        board.setupBoard(List.of(playerA, playerB));

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 4);
        assertEquals(upperCards, board.getTribeShowed().getUpperList());
    }

    @Test
    @DisplayName("setupBoard: should draw correct number of building cards for Era.FIRST")
    void shouldDisplayCorrectBuildingCardsForFirstEra() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        BuildingCard b1 = new BuildingCard("BLD-01", Era.FIRST, null, 3, 3);
        when(buildingDeck.drawCards(1)).thenReturn(List.of(b1));

        board.setupBoard(List.of(playerA, playerB));

        verify(buildingDeck, times(1)).drawCards(1);
        assertEquals(List.of(b1), board.getBuildingShowed().getUpperList());
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
    @DisplayName("endTurn: should clear the lower row after turn ends")
    void shouldClearAndShiftTribeShowed() {
        board.getTribeShowed().addLowerCards(List.of(
                new CharacterCard("L1", Era.FIRST, null, CharacterType.ARTIST, 2)));
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of());

        board.endTurn(nc, playerContext);

        assertTrue(board.getTribeShowed().getLowerList().isEmpty());
    }

    @Test
    @DisplayName("endTurn: should not throw when lower list is empty before registerBottom")
    void shouldCallRegisterBottomOnTribeShowed() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        assertDoesNotThrow(() -> board.endTurn(nc, playerContext));
    }

    @Test
    @DisplayName("endTurn: should draw and display new tribe cards after shifting")
    void shouldDisplayNewTribeCardsAfterShift() {
        CharacterCard newCard = new CharacterCard("NEW-1", Era.FIRST, null, CharacterType.ARTIST, 2);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of(newCard));

        board.endTurn(nc, playerContext);

        assertTrue(board.getTribeShowed().getUpperList().contains(newCard));
    }

    @Test
    @DisplayName("endTurn: should not change era when upper and lower cards share the same era")
    void shouldNotChangeEraWhenDiffLastErasIsFalse() {
        board.getTribeShowed().addUpperCards(List.of(
                new CharacterCard("U1", Era.FIRST, mockStrategy, CharacterType.ARTIST, 2)));

        CharacterCard nextCard = new CharacterCard("U2", Era.FIRST, mockStrategy, CharacterType.ARTIST, 2);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of(nextCard));

        board.endTurn(nc, playerContext);

        assertTrue(board.getBuildingShowed().getUpperList().isEmpty());
    }

    @Test
    @DisplayName("endTurn: should trigger era change and draw building cards when upper and lower eras differ")
    void shouldChangeEraWhenDiffLastErasIsTrue() {
        board.getTribeShowed().addUpperCards(List.of(
                new CharacterCard("U1", Era.FIRST, mockStrategy, CharacterType.ARTIST, 2)));

        CharacterCard nextEraCard = new CharacterCard("U2", Era.SECOND, mockStrategy, CharacterType.ARTIST, 2);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(List.of(nextEraCard));

        BuildingCard b = new BuildingCard("BLD-NEW", Era.SECOND, mockStrategy, 3, 3);
        when(buildingDeck.drawCards(2)).thenReturn(List.of(b));

        board.endTurn(nc, playerContext);

        verify(buildingDeck, times(1)).drawCards(2);
        assertTrue(board.getBuildingShowed().getUpperList().contains(b));
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a non-null snapshot")
    void shouldReturnNonNullSnapshot() {
        when(tribeDeck.getRemainingCardIds()).thenReturn(List.of());
        when(buildingDeck.getRemainingCardIds()).thenReturn(List.of());
        when(track.toSnapshot()).thenReturn(mock(OfferTrackSnapshot.class));
        when(turnOrder.toSnapshot()).thenReturn(mock(OfferTurnCardSnapshot.class));

        assertNotNull(board.toSnapshot());
    }

    @Test
    @DisplayName("toSnapshot: should correctly include showed card ids and deck ids")
    void shouldDelegateToAllDependenciesToBuildSnapshot() {
        CharacterCard upperTribe  = new CharacterCard("ART-01", Era.FIRST, null, CharacterType.ARTIST, 2);
        CharacterCard lowerTribe  = new CharacterCard("ART-02", Era.FIRST, null, CharacterType.ARTIST, 2);
        BuildingCard upperBuilding = new BuildingCard("BLD-01", Era.FIRST, null, 3, 3);
        BuildingCard lowerBuilding = new BuildingCard("BLD-02", Era.FIRST, null, 3, 3);

        board.getTribeShowed().addUpperCards(List.of(upperTribe));
        board.getTribeShowed().addLowerCards(List.of(lowerTribe));
        board.getBuildingShowed().addUpperCards(List.of(upperBuilding));
        board.getBuildingShowed().addLowerCards(List.of(lowerBuilding));

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