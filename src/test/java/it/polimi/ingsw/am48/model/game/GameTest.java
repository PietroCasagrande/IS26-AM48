package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;

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
        assertEquals(0, game.getCurrentTurn());
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
        assertEquals(0, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(1, game.getCurrentTurn());
        game.incrementTurn();
        assertEquals(2, game.getCurrentTurn());
    }

    // findWinner should return player with highest points
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
}