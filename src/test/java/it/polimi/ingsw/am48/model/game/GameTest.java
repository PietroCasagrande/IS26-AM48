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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("G1", 3);
    }

    // New game should be empty and not full
    @Test
    void newGameShouldBeEmptyAndNotFull() {
        assertEquals("G1", game.getGameId());
        assertEquals(3, game.getNumPlayers());
        assertEquals(0, game.getPlayerContext().getPlayers().size());
        assertFalse(game.isFull());
        assertEquals(1, game.getCurrentTurn());
    }

    // getPlayerByNickname should find existing player
    @Test
    void getPlayerByNicknameShouldFindPlayer() {
        game.getPlayerContext().addPlayer(new Player("Ilaria", Totem.RED));
        Player found = game.getPlayerByNickname("Ilaria");
        assertEquals("Ilaria", found.getNickname());
        assertEquals(Totem.RED, found.getTotem());
    }

    // getPlayerByNickname with unknown name should throw
    @Test
    void getPlayerByNicknameUnknownShouldThrow() {
        assertThrows(InvalidActionException.class, () ->
                game.getPlayerByNickname("Nobody"));
    }

    // isFull should return true when player count matches numPlayers
    @Test
    void isFullShouldReturnTrueWhenFull() {
        game.getPlayerContext().addPlayer(new Player("A", Totem.RED));
        game.getPlayerContext().addPlayer(new Player("B", Totem.BLUE));
        game.getPlayerContext().addPlayer(new Player("C", Totem.WHITE));
        assertTrue(game.isFull());
    }

    // isFull should return false when not enough players
    @Test
    void isFullShouldReturnFalseWhenNotFull() {
        game.getPlayerContext().addPlayer(new Player("A", Totem.RED));
        assertFalse(game.isFull());
    }

    // incrementTurn should increase turn counter
    @Test
    void incrementTurnShouldIncrease() {
        assertEquals(1, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(2, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(3, game.getCurrentTurn());
    }

    // findWinner should return player with the highest amount of points
    @Test
    void findWinnerShouldReturnHighestScore() {
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

    // findWinner with no players should throw
    @Test
    void findWinnerWithNoPlayersShouldThrow() {
        assertThrows(InvalidActionException.class, () -> game.findWinner());
    }

    // setPhase should change current phase
    @Test
    void setPhaseShouldChangePhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        assertEquals(mockPhase, game.getCurrentPhase());
    }
    @Test
    @DisplayName("getNotificatorCenter: should return the notification center instance")
    void shouldReturnNotificatorCenter() {
        assertNotNull(game.getNotificatorCenter());
    }

    @Test
    @DisplayName("setBoard and getBoard: should correctly handle the board instance")
    void shouldSetAndGetBoard() {
        Board mockBoard = mock(Board.class);
        game.setBoard(mockBoard);
        assertEquals(mockBoard, game.getBoard());
    }

    @Test
    @DisplayName("addPlayer: should delegate call to current phase")
    void addPlayerShouldDelegateToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);

        game.addPlayer("TestPlayer");

        verify(mockPhase).addPlayer(game.getPlayerContext(), game, "TestPlayer");
    }

    @Test
    @DisplayName("placeTotem: should delegate call to current phase")
    void placeTotemShouldDelegateToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        Player mockPlayer = mock(Player.class);

        game.placeTotem(mockPlayer, 'A');

        verify(mockPhase).placeTotem(game, mockPlayer, 'A');
    }

    @Test
    @DisplayName("takeCard: should delegate call to current phase")
    void takeCardShouldDelegateToPhase() {
        GamePhase mockPhase = mock(GamePhase.class);
        game.setPhase(mockPhase);
        Player mockPlayer = mock(Player.class);

        game.takeCard(mockPlayer, "card_01");

        verify(mockPhase).takeCard(game, mockPlayer, "card_01");
    }

    @Test
    @DisplayName("toSnapshot: should create snapshot with null board when board is not set")
    void toSnapshotShouldHandleNullBoard() {
        // Durante WaitingForPlayersPhase la board è null
        GameSnapshot snapshot = game.toSnapshot();

        assertAll("Snapshot with null board",
                () -> assertEquals(game.getGameId(), snapshot.getGameId()),
                () -> assertEquals(game.getCurrentTurn(), snapshot.getCurrentTurn()),
                () -> assertNull(snapshot.getBoard()),
                () -> assertNotNull(snapshot.getPlayers())
        );
    }

//    @Test
//    @DisplayName("toSnapshot: should create complete snapshot when board is present")
//    void toSnapshotShouldIncludeBoard() {
//        // Arrange
//        Board mockBoard = mock(Board.class);
//        game.setBoard(mockBoard);
//        game.getPlayerContext().addPlayer(new Player("Ilaria", Totem.RED));
//
//        // Act
//        GameSnapshot snapshot = game.toSnapshot();
//
//        // Assert
//        assertAll("Complete snapshot",
//                () -> assertEquals(1, snapshot.getPlayers().size()),
//                () -> assertNotNull(snapshot.getBoard()),
//                () -> assertEquals(game.getGameId(), snapshot.getGameId())
//        );
//    }
}