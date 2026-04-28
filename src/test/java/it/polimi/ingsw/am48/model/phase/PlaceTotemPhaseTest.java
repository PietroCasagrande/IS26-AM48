package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.OfferCardADelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.notificator.OnTotemReturnedNotificator;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaceTotemPhaseTest {

    private Game game;
    private Board board;
    private Player playerA;
    private Player playerB;
    private Player playerC;
    private PlaceTotemPhase phase;

    @BeforeEach
    void setUp() {
        game    = mock(Game.class);
        board   = mock(Board.class);
        playerA = mock(Player.class);
        playerB = mock(Player.class);
        playerC = mock(Player.class);

        PlayerContext mockPlayerContext = mock(PlayerContext.class);
        NotificatorCenter mockNotificatorCenter = mock(NotificatorCenter.class);
        OnTotemReturnedNotificator mockTotemReturnedNotificator = mock(OnTotemReturnedNotificator.class);

        when(game.getPlayerContext()).thenReturn(mockPlayerContext);
        when(game.getNotificatorCenter()).thenReturn(mockNotificatorCenter);
        when(mockNotificatorCenter.getTotemReturnedNotificator()).thenReturn(mockTotemReturnedNotificator);

        // Mock a normal offer card (not tile A)
        OfferCard normalOffer = mock(OfferCard.class);
        when(normalOffer.getLetterId()).thenReturn('B');

        when(game.getBoard()).thenReturn(board);
        when(board.findTrackPosition(any())).thenReturn(normalOffer);

        when(playerA.getNickname()).thenReturn("alice");
        when(playerB.getNickname()).thenReturn("bob");
        when(playerC.getNickname()).thenReturn("charlie");

        phase = new PlaceTotemPhase();
    }

    // ==================== placeTotem - validazione ====================

    @Test
    @DisplayName("placeTotem: should throw InvalidActionException when player places totem twice")
    void shouldThrowWhenPlayerPlacesTotemTwice() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        phase.placeTotem(game, playerA, 'B');

        assertThrows(InvalidActionException.class,
                () -> phase.placeTotem(game, playerA, 'C'));
    }

    @Test
    @DisplayName("placeTotem: should not throw when different players place their totems")
    void shouldNotThrowWhenDifferentPlayersPlaceTheirTotems() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        assertDoesNotThrow(() -> phase.placeTotem(game, playerA, 'B'));
        assertDoesNotThrow(() -> phase.placeTotem(game, playerB, 'C'));
    }

    @Test
    @DisplayName("placeTotem: should throw InvalidActionException with descriptive message")
    void shouldThrowWithDescriptiveMessageWhenAlreadyPlaced() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        phase.placeTotem(game, playerA, 'B');

        InvalidActionException ex = assertThrows(InvalidActionException.class,
                () -> phase.placeTotem(game, playerA, 'C'));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    // ==================== placeTotem - delega a Board ====================

    @Test
    @DisplayName("placeTotem: should delegate to board.placeTotem with correct arguments")
    void shouldDelegatePlaceTotemToBoardWithCorrectArguments() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        phase.placeTotem(game, playerA, 'B');

        verify(board, times(1)).placeTotem(playerA, 'B');
    }

    @Test
    @DisplayName("placeTotem: should delegate to board for each player that places their totem")
    void shouldDelegateToBoarForEachPlayerThatPlacesTotem() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');

        verify(board, times(1)).placeTotem(playerA, 'B');
        verify(board, times(1)).placeTotem(playerB, 'C');
    }

    // ==================== placeTotem - delta restituito ====================

    @Test
    @DisplayName("placeTotem: should return a TotemPlacedDelta")
    void shouldReturnTotemPlacedDelta() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        GameDelta delta = phase.placeTotem(game, playerA, 'B').getFirst();

        assertInstanceOf(TotemPlacedDelta.class, delta);
    }

    @Test
    @DisplayName("placeTotem: should return delta with correct player nickname")
    void shouldReturnDeltaWithCorrectPlayerNickname() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        TotemPlacedDelta delta = (TotemPlacedDelta) phase.placeTotem(game, playerA, 'B').getFirst();

        assertEquals("alice", delta.getPlayerNickname());
    }

    @Test
    @DisplayName("placeTotem: should return delta with correct position")
    void shouldReturnDeltaWithCorrectPosition() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        TotemPlacedDelta delta = (TotemPlacedDelta) phase.placeTotem(game, playerA, 'B').getFirst();

        assertEquals('B', delta.getTileId());
    }

    // ==================== placeTotem - dimensione lista delta ====================

    @Test
    @DisplayName("placeTotem: should return list with exactly one delta when player is not the last")
    void shouldReturnSingleDeltaWhenPlayerIsNotLast() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        List<GameDelta> deltas = phase.placeTotem(game, playerA, 'B');

        assertEquals(1, deltas.size());
        assertInstanceOf(TotemPlacedDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("placeTotem: should return list with exactly one delta when last player places and nobody is on tile A")
    void shouldReturnSingleDeltaWhenLastPlayerPlacesAndNoTileA() {
        // nessuno sulla tessera A - setup restituisce Optional.empty()
        OfferCard nonATile = mock(OfferCard.class);
        when(nonATile.getLetterId()).thenReturn('B');
        when(board.findTrackPosition(any())).thenReturn(nonATile);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(game.getNumPlayers()).thenReturn(3);

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');
        List<GameDelta> deltas = phase.placeTotem(game, playerC, 'D');

        assertEquals(1, deltas.size());
        assertInstanceOf(TotemPlacedDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("placeTotem: should return list with exactly two deltas when last player places and first in order is on tile A")
    void shouldReturnTwoDeltasWhenLastPlayerPlacesAndFirstIsOnTileA() {
        // playerA è il primo nell'ordine di pesca ed è sulla tessera A
        OfferCard tileA = mock(OfferCard.class);
        when(tileA.getLetterId()).thenReturn('A');
        when(tileA.getFoodBonus()).thenReturn(3);

        OfferCard tileB = mock(OfferCard.class);
        when(tileB.getLetterId()).thenReturn('B');

        // findTrackPosition restituisce A solo per playerA
        when(board.findTrackPosition(playerA)).thenReturn(tileA);
        when(board.findTrackPosition(playerB)).thenReturn(tileB);
        when(board.findTrackPosition(playerC)).thenReturn(tileB);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(board.getPlaceOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(game.getNumPlayers()).thenReturn(3);
        when(playerA.getFood()).thenReturn(3);

        phase.placeTotem(game, playerA, 'A');
        phase.placeTotem(game, playerB, 'B');
        List<GameDelta> deltas = phase.placeTotem(game, playerC, 'C');

        assertEquals(2, deltas.size());
    }

    @Test
    @DisplayName("placeTotem: first delta should be TotemPlacedDelta when tile A is present")
    void shouldHaveTotemPlacedDeltaFirstWhenTileAIsPresent() {
        OfferCard tileA = mock(OfferCard.class);
        when(tileA.getLetterId()).thenReturn('A');
        when(tileA.getFoodBonus()).thenReturn(3);

        OfferCard tileB = mock(OfferCard.class);
        when(tileB.getLetterId()).thenReturn('B');

        when(board.findTrackPosition(playerA)).thenReturn(tileA);
        when(board.findTrackPosition(playerB)).thenReturn(tileB);
        when(board.findTrackPosition(playerC)).thenReturn(tileB);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(board.getPlaceOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(game.getNumPlayers()).thenReturn(3);
        when(playerA.getFood()).thenReturn(3);

        phase.placeTotem(game, playerA, 'A');
        phase.placeTotem(game, playerB, 'B');
        List<GameDelta> deltas = phase.placeTotem(game, playerC, 'C');

        assertInstanceOf(TotemPlacedDelta.class, deltas.get(0));
    }

    @Test
    @DisplayName("placeTotem: second delta should be OfferCardADelta when tile A is present")
    void shouldHaveOfferCardADeltaSecondWhenTileAIsPresent() {
        OfferCard tileA = mock(OfferCard.class);
        when(tileA.getLetterId()).thenReturn('A');
        when(tileA.getFoodBonus()).thenReturn(3);

        OfferCard tileB = mock(OfferCard.class);
        when(tileB.getLetterId()).thenReturn('B');

        when(board.findTrackPosition(playerA)).thenReturn(tileA);
        when(board.findTrackPosition(playerB)).thenReturn(tileB);
        when(board.findTrackPosition(playerC)).thenReturn(tileB);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(board.getPlaceOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(game.getNumPlayers()).thenReturn(3);
        when(playerA.getFood()).thenReturn(3);

        phase.placeTotem(game, playerA, 'A');
        phase.placeTotem(game, playerB, 'B');
        List<GameDelta> deltas = phase.placeTotem(game, playerC, 'C');

        assertInstanceOf(OfferCardADelta.class, deltas.get(1));
    }

    @Test
    @DisplayName("placeTotem: OfferCardADelta should contain correct nickname and food")
    void shouldHaveCorrectDataInOfferCardADelta() {
        OfferCard tileA = mock(OfferCard.class);
        when(tileA.getLetterId()).thenReturn('A');
        when(tileA.getFoodBonus()).thenReturn(3);

        OfferCard tileB = mock(OfferCard.class);
        when(tileB.getLetterId()).thenReturn('B');

        when(board.findTrackPosition(playerA)).thenReturn(tileA);
        when(board.findTrackPosition(playerB)).thenReturn(tileB);
        when(board.findTrackPosition(playerC)).thenReturn(tileB);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(board.getPlaceOrder()).thenReturn(List.of(playerA, playerB, playerC));
        when(game.getNumPlayers()).thenReturn(3);
        when(playerA.getNickname()).thenReturn("alice");
        when(playerA.getFood()).thenReturn(3);

        phase.placeTotem(game, playerA, 'A');
        phase.placeTotem(game, playerB, 'B');
        List<GameDelta> deltas = phase.placeTotem(game, playerC, 'C');

        OfferCardADelta aDelta = (OfferCardADelta) deltas.get(1);
        assertEquals("alice", aDelta.getPlayerNickname());
        assertEquals(3, aDelta.getUpdatedFood());
    }

    // ==================== placeTotem - transizione di fase ====================

    @Test
    @DisplayName("placeTotem: should not transition phase before all players have placed")
    void shouldNotTransitionPhaseBeforeAllPlayersHavePlaced() {
        when(game.getNumPlayers()).thenReturn(3);

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');

        // only 2 out of 3 placed: phase transition must not happen yet
        verify(game, never()).setPhase(any());
    }

    @Test
    @DisplayName("placeTotem: should transition to PlayerOfferPhase when all players have placed")
    void shouldTransitionToPlayerOfferPhaseWhenAllPlayersHavePlaced() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');
        phase.placeTotem(game, playerC, 'D');

        verify(game, times(1)).setPhase(any(PlayerOfferPhase.class));
    }

    @Test
    @DisplayName("placeTotem: should pass board action order to PlayerOfferPhase on transition")
    void shouldPassBoardActionOrderToPlayerOfferPhaseOnTransition() {
        List<Player> trackOrder = List.of(playerC, playerA, playerB);
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(trackOrder);

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');
        phase.placeTotem(game, playerC, 'D');

        verify(board, times(1)).getPickOrder();
        verify(game, times(1)).setPhase(any(PlayerOfferPhase.class));
    }

    @Test
    @DisplayName("placeTotem: should query board action order only when all players have placed")
    void shouldQueryBoardActionOrderOnlyWhenAllPlayersHavePlaced() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of(playerA, playerB, playerC));

        phase.placeTotem(game, playerA, 'B');
        phase.placeTotem(game, playerB, 'C');

        verify(board, never()).getPickOrder();

        phase.placeTotem(game, playerC, 'D');

        verify(board, times(1)).getPickOrder();
    }
}