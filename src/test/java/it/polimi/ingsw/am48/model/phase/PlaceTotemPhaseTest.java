package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
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

        GameDelta delta = phase.placeTotem(game, playerA, 'B');

        assertInstanceOf(TotemPlacedDelta.class, delta);
    }

    @Test
    @DisplayName("placeTotem: should return delta with correct player nickname")
    void shouldReturnDeltaWithCorrectPlayerNickname() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        TotemPlacedDelta delta = (TotemPlacedDelta) phase.placeTotem(game, playerA, 'B');

        assertEquals("alice", delta.getPlayerNickname());
    }

    @Test
    @DisplayName("placeTotem: should return delta with correct position")
    void shouldReturnDeltaWithCorrectPosition() {
        when(game.getNumPlayers()).thenReturn(3);
        when(board.getPickOrder()).thenReturn(List.of());

        TotemPlacedDelta delta = (TotemPlacedDelta) phase.placeTotem(game, playerA, 'B');

        assertEquals('B', delta.getTileId());
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