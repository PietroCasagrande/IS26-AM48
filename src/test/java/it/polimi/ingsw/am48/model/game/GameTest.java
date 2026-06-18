package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("G1", 3);
    }

    @Test
    @DisplayName("new game should have correct initial state")
    void newGameShouldBeEmptyAndNotFull() {
        assertEquals("G1", game.getGameId());
        assertEquals(3, game.getNumPlayers());
        assertEquals(0, game.getPlayerContext().getPlayers().size());
        assertFalse(game.isFull());
        assertEquals(1, game.getCurrentTurn());
    }

    @Test
    @DisplayName("getPlayerByNickname: should find existing player")
    void shouldGetPlayerByNickname() {
        game.getPlayerContext().addPlayer(new Player("Ilaria", Totem.RED));
        Player found = game.getPlayerByNickname("Ilaria");
        assertEquals("Ilaria", found.getNickname());
        assertEquals(Totem.RED, found.getTotem());
    }

    @Test
    @DisplayName("getPlayerByNickname: should throw InvalidActionException for unknown nickname")
    void shouldThrowForUnknownNickname() {
        assertThrows(InvalidActionException.class, () -> game.getPlayerByNickname("Nobody"));
    }

    @Test
    @DisplayName("isFull: should return true when player count matches numPlayers")
    void shouldReturnTrueWhenFull() {
        game.getPlayerContext().addPlayer(new Player("A", Totem.RED));
        game.getPlayerContext().addPlayer(new Player("B", Totem.BLUE));
        game.getPlayerContext().addPlayer(new Player("C", Totem.WHITE));
        assertTrue(game.isFull());
    }

    @Test
    @DisplayName("isFull: should return false when not enough players")
    void shouldReturnFalseWhenNotFull() {
        game.getPlayerContext().addPlayer(new Player("A", Totem.RED));
        assertFalse(game.isFull());
    }

    @Test
    @DisplayName("incrementTurn: should increase the turn counter")
    void shouldIncrementTurn() {
        assertEquals(1, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(2, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(3, game.getCurrentTurn());
    }

    @Test
    @DisplayName("findWinner: should return player with highest points")
    void shouldFindWinnerWithHighestScore() {
        Player p1 = new Player("A", Totem.RED);
        Player p2 = new Player("B", Totem.BLUE);
        Player p3 = new Player("C", Totem.WHITE);
        p1.updatePoints(10);
        p2.updatePoints(25);
        p3.updatePoints(15);
        game.getPlayerContext().addPlayer(p1);
        game.getPlayerContext().addPlayer(p2);
        game.getPlayerContext().addPlayer(p3);
        assertEquals("B", game.findWinner().getNickname());
    }

    @Test
    @DisplayName("findWinner: should throw InvalidActionException when no players present")
    void shouldThrowWhenNoPlayers() {
        assertThrows(InvalidActionException.class, () -> game.findWinner());
    }

    @Test
    @DisplayName("setPhase: should change the current phase")
    void shouldSetPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        assertEquals(mockPhase, game.getCurrentPhase());
    }

    @Test
    @DisplayName("getNotificatorCenter: should return a non-null instance")
    void shouldReturnNonNullNotificatorCenter() {
        assertNotNull(game.getNotificatorCenter());
    }

    @Test
    @DisplayName("setBoard/getBoard: should correctly store and retrieve the board")
    void shouldSetAndGetBoard() {
        Board mockBoard = mock(Board.class);
        game.setBoard(mockBoard);
        assertEquals(mockBoard, game.getBoard());
    }

    @Test
    @DisplayName("addPlayer: should delegate to current phase")
    void shouldDelegateAddPlayerToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);

        game.addPlayer("TestPlayer");

        verify(mockPhase).addPlayer(game.getPlayerContext(), game, "TestPlayer");
    }

    @Test
    @DisplayName("placeTotem: should delegate to current phase")
    void shouldDelegatePlaceTotemToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        Player mockPlayer = mock(Player.class);

        game.placeTotem(mockPlayer, 'A');

        verify(mockPhase).placeTotem(game, mockPlayer, 'A');
    }

    @Test
    @DisplayName("takeCard: should delegate to current phase")
    void shouldDelegateTakeCardToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        Player mockPlayer = mock(Player.class);

        game.takeCard(mockPlayer, "card_01");

        verify(mockPhase).takeCard(game, mockPlayer, "card_01");
    }

    @Test
    @DisplayName("toSnapshot: should produce snapshot with null board before board is set")
    void shouldHandleNullBoardInSnapshot() {
        GameSnapshot snapshot = game.toSnapshot();

        assertAll(
                () -> assertEquals(game.getGameId(), snapshot.getGameId()),
                () -> assertEquals(game.getCurrentTurn(), snapshot.getCurrentTurn()),
                () -> assertNull(snapshot.getBoard()),
                () -> assertNotNull(snapshot.getPlayerContext().getPlayers())
        );
    }

    @Test
    @DisplayName("toSnapshot: should include board snapshot when board is set")
    void shouldIncludeBoardInSnapshot() {
        Board mockBoard = mock(Board.class);
        when(mockBoard.toSnapshot()).thenReturn(mock(
                it.polimi.ingsw.am48.model.snapshot.BoardSnapshot.class));
        game.setBoard(mockBoard);
        game.getPlayerContext().addPlayer(new Player("Ilaria", Totem.RED));

        GameSnapshot snapshot = game.toSnapshot();

        assertAll(
                () -> assertEquals(1, snapshot.getPlayerContext().getPlayers().size()),
                () -> assertNotNull(snapshot.getBoard()),
                () -> assertEquals(game.getGameId(), snapshot.getGameId())
        );
    }
}