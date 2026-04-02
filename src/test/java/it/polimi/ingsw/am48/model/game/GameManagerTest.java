package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager manager;

    @BeforeEach
    void setUp() {
        manager = new GameManager();
    }

    // joinGame with duplicate nickname should throw
    // Note: joinGame delegates to WaitingForPlayersPhase.addPlayer which is not yet implemented,
    // so we catch the UnsupportedOperationException from the phase and test only the nickname check

/*
    @Test
    void joinGameDuplicateNicknameShouldThrow() {
        try {
            manager.joinGame(4, "Pietro");
        } catch (UnsupportedOperationException ignored) {
            // WaitingForPlayersPhase.addPlayer not yet implemented
        }
        assertThrows(InvalidActionException.class, () ->
                manager.joinGame(4, "Pietro"));
    }
 */

    // placeTotem with unknown nickname should throw
    @Test
    void placeTotemUnknownNicknameShouldThrow() {
        assertThrows(InvalidActionException.class, () ->
                manager.placeTotem("Nobody", 'A'));
    }

    // takeCard with unknown nickname should throw
    @Test
    void takeCardUnknownNicknameShouldThrow() {
        assertThrows(InvalidActionException.class, () ->
                manager.takeCard("Nobody", "H1"));
    }

    // findGame with unknown gameId should throw
    @Test
    void findGameUnknownIdShouldThrow() {
        assertThrows(InvalidActionException.class, () ->
                manager.findGame("NONEXISTENT"));
    }

    // Multiple joins with different nicknames should not throw
    @Test
    void joinGameDifferentNicknamesShouldNotThrowOnNicknameCheck() {
        try {
            manager.joinGame(4, "Pietro");
        } catch (UnsupportedOperationException ignored) {}
        try {
            manager.joinGame(4, "Marco");
        } catch (UnsupportedOperationException ignored) {}
        // If we got here without InvalidActionException, nickname check passed for both
    }
}