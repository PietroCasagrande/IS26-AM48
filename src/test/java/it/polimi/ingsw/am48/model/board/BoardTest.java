package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
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

    private Player playerA;
    private Player playerB;
    private Card cardA;
    private BuildingCard buildingCardA;

    private static final int NUM_PLAYERS = 2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        track = mock(OfferCardTrack.class);
        turnOrder = mock(OfferTurnCard.class);
        tribeDeck = mock(Deck.class);
        buildingDeck = mock(Deck.class);
        tribeShowed = mock(Showed.class);
        buildingShowed = mock(Showed.class);
        buildingsPerEra = List.of(1, 2, 3); // one value per era

        playerA = mock(Player.class);
        playerB = mock(Player.class);
        cardA = mock(Card.class);
        buildingCardA = mock(BuildingCard.class);

        board = new Board(track, turnOrder, tribeDeck, buildingDeck,
                tribeShowed, buildingShowed, buildingsPerEra, NUM_PLAYERS);
    }

    // ==================== placeTotem ====================

    @Test
    @DisplayName("placeTotem: delegates to track with correct arguments")
    void shouldDelegatePlacingTotemToTrack() {
        board.placeTotem(playerA, 'A');
        verify(track, times(1)).placeTotem(playerA, 'A');
    }

    @Test
    @DisplayName("placeTotem: does not interact with any other dependency")
    void shouldOnlyInteractWithTrackForPlaceTotem() {
        board.placeTotem(playerA, 'A');
        verifyNoInteractions(turnOrder, tribeDeck, buildingDeck, tribeShowed, buildingShowed);
    }

    // ==================== getActionOrder ====================

    @Test
    @DisplayName("getActionOrder: delegates to track and returns its result")
    void shouldGetCorrectActionOrderFromTrack() {
        List<Player> expected = List.of(playerA, playerB);
        when(track.getActionOrder()).thenReturn(expected);

        List<Player> result = board.getActionOrder();

        assertEquals(expected, result);
        verify(track, times(1)).getActionOrder();
    }

    @Test
    @DisplayName("getActionOrder: does not interact with any other dependency")
    void shouldOnlyInteractWithTrackForActionOrder() {
        when(track.getActionOrder()).thenReturn(List.of());
        board.getActionOrder();
        verifyNoInteractions(turnOrder, tribeDeck, buildingDeck, tribeShowed, buildingShowed);
    }

    // ==================== takeCard ====================

    @Test
    @DisplayName("takeCard: returns card from tribeShowed when found there")
    void shouldReturnCardFromTribeShowed() {
        when(tribeShowed.takeCard(playerA, "ART-01")).thenReturn(Optional.of(cardA));

        Card result = board.takeCard(playerA, "ART-01");

        assertEquals(cardA, result);
        verify(tribeShowed, times(1)).takeCard(playerA, "ART-01");
    }

    @Test
    @DisplayName("takeCard: falls back to buildingShowed when card not in tribeShowed")
    void shouldFindInBuildingShowedIfNotInTribeShowed() {
        when(tribeShowed.takeCard(playerA, "BUI-01")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "BUI-01")).thenReturn(Optional.of(buildingCardA));

        Card result = board.takeCard(playerA, "BUI-01");

        assertEquals(buildingCardA, result);
        verify(tribeShowed, times(1)).takeCard(playerA, "BUI-01");
        verify(buildingShowed, times(1)).takeCard(playerA, "BUI-01");
    }

    @Test
    @DisplayName("takeCard: throws IllegalArgumentException when card not found in either showed")
    void shouldThrowIllegalArgumentExceptionWhenCardNotFound() {
        when(tribeShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> board.takeCard(playerA, "unknown"));
    }

    @Test
    @DisplayName("takeCard: exception message contains the card id")
    void shouldExceptionMessageContainCardId() {
        when(tribeShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());
        when(buildingShowed.takeCard(playerA, "unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> board.takeCard(playerA, "unknown"));
        assertTrue(ex.getMessage().contains("unknown"));
    }

    // ==================== isCardTop ====================

    @Test
    @DisplayName("isCardTop: returns true when card is on top row of tribeShowed")
    void shouldReturnTrueForTribeShowedIfTop() {
        when(tribeShowed.isTop("ART-01")).thenReturn(true);

        assertTrue(board.isCardTop("ART-01"));
    }

    @Test
    @DisplayName("isCardTop: returns true when card is on top row of buildingShowed")
    void shouldReturnTrueForBuildingShowedIfTop() {
        when(tribeShowed.isTop("BUI-01")).thenReturn(false);
        when(buildingShowed.isTop("BUI-01")).thenReturn(true);

        assertTrue(board.isCardTop("BUI-01"));
    }

    @Test
    @DisplayName("isCardTop: returns false when card is not on top row of either showed")
    void shouldReturnFalseWhenNotFoundInTop() {
        when(tribeShowed.isTop("unknown")).thenReturn(false);
        when(buildingShowed.isTop("unknown")).thenReturn(false);

        assertFalse(board.isCardTop("unknown"));
    }

    // ==================== isCardDown ====================

    @Test
    @DisplayName("isCardDown: returns true when card is on lower row of tribeShowed")
    void shouldReturnTrueForTribeShowedIfDown() {
        when(tribeShowed.isDown("ART-01")).thenReturn(true);

        assertTrue(board.isCardDown("ART-01"));
    }

    @Test
    @DisplayName("isCardDown: returns true when card is on lower row of buildingShowed")
    void shouldReturnTrueForBuildingShowedIfDown() {
        when(tribeShowed.isDown("BUI-01")).thenReturn(false);
        when(buildingShowed.isDown("BUI-01")).thenReturn(true);

        assertTrue(board.isCardDown("BUI-01"));
    }

    @Test
    @DisplayName("isCardDown: returns false when card is not on lower row of either showed")
    void shouldReturnFalseWhenNotFoundInBottom() {
        when(tribeShowed.isDown("unknown")).thenReturn(false);
        when(buildingShowed.isDown("unknown")).thenReturn(false);

        assertFalse(board.isCardDown("unknown"));
    }

    // ==================== findTrackPosition ====================

    @Test
    @DisplayName("findTrackPosition: delegates to track and returns its result")
    void shouldDelegateFindTrackPositionToTrack() {
        OfferCard offerCard = mock(OfferCard.class);
        when(track.findTrackPosition(playerA)).thenReturn(offerCard);

        OfferCard result = board.findTrackPosition(playerA);

        assertEquals(offerCard, result);
        verify(track, times(1)).findTrackPosition(playerA);
    }

    @Test
    @DisplayName("findTrackPosition: returns null when player has no totem on track")
    void shouldReturnNullWhenPlayerNotFound() {
        when(track.findTrackPosition(playerA)).thenReturn(null);

        assertNull(board.findTrackPosition(playerA));
    }

    // ==================== setupBoard ====================

    @Test
    @DisplayName("setupBoard: draws numPlayers+1 tribe cards and displays them on lower row")
    void shouldDisplayCorrectNumberOfTribeCardsOnLowerRow() {
        List<Card> lowerCards = List.of(mock(Card.class), mock(Card.class), mock(Card.class));
        List<Card> upperCards = List.of(mock(Card.class), mock(Card.class),
                mock(Card.class), mock(Card.class), mock(Card.class), mock(Card.class));
        // first draw: numPlayers+1=3, second draw (displayTribeCards): numPlayers+4=6
        when(tribeDeck.drawCards(NUM_PLAYERS + 1)).thenReturn(lowerCards);
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(upperCards);
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        board.setupBoard(List.of(playerA, playerB));

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 1);
        verify(tribeShowed, times(1)).addLowerCards(lowerCards);
    }

    @Test
    @DisplayName("setupBoard: draws numPlayers+4 tribe cards and displays them on upper row")
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
    @DisplayName("setupBoard: draws correct number of building cards for Era.FIRST")
    void shouldDisplayCorrectBuildingCardsForFirstEra() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        List<BuildingCard> buildings = List.of(mock(BuildingCard.class),
                mock(BuildingCard.class), mock(BuildingCard.class));
        when(buildingDeck.drawCards(buildingsPerEra.get(Era.FIRST.getIndex()))).thenReturn(buildings);

        board.setupBoard(List.of(playerA, playerB));

        verify(buildingDeck, times(1)).drawCards(1);
        verify(buildingShowed, times(1)).addUpperCards(buildings);
    }

    @Test
    @DisplayName("setupBoard: delegates turn order setup to turnOrder")
    void shouldDelegateTurnOrderToOfferTurnCard() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());

        List<Player> players = List.of(playerA, playerB);
        board.setupBoard(players);

        verify(turnOrder, times(1)).setupOrder(players);
    }

    // ==================== endTurn ====================

    @Test
    @DisplayName("endTurn: clears bottom row and shifts tribe showed")
    void shouldClearAndShiftTribeShowed() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn();

        verify(tribeShowed, times(1)).clearBottom();
        verify(tribeShowed, times(1)).shiftRow();
    }

    @Test
    @DisplayName("endTurn: draws and displays new tribe cards after shifting")
    void shouldDisplayNewTribeCards() {
        List<Card> newCards = List.of(mock(Card.class));
        when(tribeDeck.drawCards(NUM_PLAYERS + 4)).thenReturn(newCards);
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn();

        verify(tribeDeck, times(1)).drawCards(NUM_PLAYERS + 4);
        verify(tribeShowed, times(1)).addUpperCards(newCards);
    }

    @Test
    @DisplayName("endTurn: does not change era when diffLastEras returns false")
    void shouldNotChangeEraWhenNotNeeded() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(false);

        board.endTurn();

        verify(buildingShowed, never()).clearBottom();
        verify(buildingShowed, never()).shiftRow();
        verify(buildingDeck, never()).drawCards(anyInt());
    }

    @Test
    @DisplayName("endTurn: triggers era change when diffLastEras returns true")
    void shouldChangeEraWhenDiffLastEras() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(buildingDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(true);

        board.endTurn();

        verify(buildingShowed, times(1)).clearBottom();
        verify(buildingShowed, times(1)).shiftRow();
        verify(buildingDeck, times(1)).drawCards(anyInt());
    }

    @Test
    @DisplayName("endTurn: on era change, draws building cards for the next era")
    void shouldDrawBuildingCardsForNextEra() {
        when(tribeDeck.drawCards(anyInt())).thenReturn(List.of());
        when(tribeShowed.diffLastEras()).thenReturn(true);
        List<BuildingCard> newBuildings = List.of(mock(BuildingCard.class), mock(BuildingCard.class));
        // Era advances from FIRST to SECOND
        when(buildingDeck.drawCards(buildingsPerEra.get(Era.SECOND.getIndex()))).thenReturn(newBuildings);

        board.endTurn();

        verify(buildingDeck, times(1)).drawCards(buildingsPerEra.get(Era.SECOND.getIndex()));
        verify(buildingShowed, times(1)).addUpperCards(newBuildings);
    }

    // ==================== Getters ====================

    @Test
    @DisplayName("getTribeShowed: returns the tribeShowed instance")
    void shouldReturnCorrectInstanceOfTribeShowed() {
        assertEquals(tribeShowed, board.getTribeShowed());
    }

    @Test
    @DisplayName("getBuildingShowed: returns the buildingShowed instance")
    void shouldReturnCorrectInstanceOfBuildingShowed() {
        assertEquals(buildingShowed, board.getBuildingShowed());
    }
}