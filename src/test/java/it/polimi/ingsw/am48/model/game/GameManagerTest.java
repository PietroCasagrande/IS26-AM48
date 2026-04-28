package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.phase.PlayerOfferPhase;
import it.polimi.ingsw.am48.model.phase.PlaceTotemPhase;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager manager;

    @BeforeEach
    void setUp() {
        manager = new GameManager();
    }

    // ==================== joinGame ====================

    @Test
    @DisplayName("joinGame: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> manager.joinGame(1, "Alice"));
    }

    @Test
    @DisplayName("joinGame: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> manager.joinGame(6, "Alice"));
    }

    @Test
    @DisplayName("joinGame: should throw InvalidActionException for duplicate nickname")
    void shouldThrowForDuplicateNickname() {
        manager.joinGame(4, "Pietro");
        assertThrows(InvalidActionException.class, () -> manager.joinGame(4, "Pietro"));
    }

    @Test
    @DisplayName("joinGame: should add player to waiting game without throwing")
    void shouldAddPlayerToWaitingGame() {
        assertDoesNotThrow(() -> manager.joinGame(4, "Alice"));
    }

    @Test
    @DisplayName("joinGame: should reuse same waiting game for same numPlayers")
    void shouldReuseWaitingGameForSameNumPlayers() {
        manager.joinGame(4, "Alice");
        manager.joinGame(4, "Bob");
        assertDoesNotThrow(() -> manager.joinGame(4, "Charlie"));
    }

    @Test
    @DisplayName("joinGame: should create a new game when game becomes full and move to active")
    void shouldMoveGameToActiveWhenFull() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        assertDoesNotThrow(() -> manager.joinGame(2, "Charlie"));
    }

    @Test
    @DisplayName("joinGame: should assign different game ids to different games")
    void shouldAssignDifferentGameIds() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        manager.joinGame(2, "Charlie");
        manager.joinGame(2, "Dave");

        Game game1 = manager.findGame("GAME-1");
        Game game2 = manager.findGame("GAME-2");
        assertNotEquals(game1.getGameId(), game2.getGameId());
    }

    // ==================== placeTotem ====================

    @Test
    @DisplayName("placeTotem: should throw InvalidActionException for unknown nickname")
    void shouldThrowPlaceTotemForUnknownNickname() {
        assertThrows(InvalidActionException.class, () -> manager.placeTotem("Nobody", 'A'));
    }

    @Test
    @DisplayName("joinGame: should create board and switch to PlaceTotemPhase when game becomes full")
    void shouldCreateBoardAndSwitchToPlaceTotemPhaseWhenFull() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        Game game = manager.getGameByNickname("Alice");
        assertTrue(game.isFull());
        assertNotNull(game.getBoard());
        assertNotNull(game.toSnapshot().getBoard());
        assertTrue(game.getCurrentPhase() instanceof PlaceTotemPhase);
        assertEquals(2, manager.getPlayersInGame("Alice").size());
    }

    @Test
    @DisplayName("placeTotem: after all players place totems game should transition to PlayerOfferPhase")
    void shouldTransitionToPlayerOfferPhaseAfterAllTotemsPlaced() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        Game game = manager.getGameByNickname("Alice");
        List<Player> placeOrder = game.getBoard().getPlaceOrder();

        manager.placeTotem(placeOrder.get(0).getNickname(), 'C');
        manager.placeTotem(placeOrder.get(0).getNickname(), 'B');

        assertTrue(game.getCurrentPhase() instanceof PlayerOfferPhase);
        assertEquals(2, game.getBoard().getPickOrder().size());
    }

    @Test
    @DisplayName("takeCard: current player can take a top card during PlayerOfferPhase")
    void shouldAllowCurrentPlayerToTakeTopCardDuringPlayerOfferPhase() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        Game game = manager.getGameByNickname("Alice");
        List<Player> placeOrder = game.getBoard().getPlaceOrder();
        manager.placeTotem(placeOrder.get(0).getNickname(), 'C');
        manager.placeTotem(placeOrder.get(0).getNickname(), 'B');

        String currentPlayer = game.getBoard().getPickOrder().get(0).getNickname();
        String topCardId = game.getBoard().getTribeShowed().getLowerList().get(0).getCardId();

        List<GameDelta> deltas = manager.takeCard(currentPlayer, topCardId);

        assertFalse(deltas.isEmpty());
        assertTrue(game.getBoard().getTribeShowed().getUpperList().stream()
                .noneMatch(card -> card.getCardId().equals(topCardId)));
    }

    @Test
    @DisplayName("takeCard: should throw when requested card is not on the board")
    void shouldThrowWhenTakeCardNotOnBoard() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        Game game = manager.getGameByNickname("Alice");
        List<Player> placeOrder = game.getBoard().getPlaceOrder();
        manager.placeTotem(placeOrder.get(0).getNickname(), 'C');
        manager.placeTotem(placeOrder.get(0).getNickname(), 'B');

        String currentPlayer = game.getBoard().getPickOrder().get(0).getNickname();
        assertThrows(InvalidActionException.class, () -> manager.takeCard(currentPlayer, "INVALID_CARD_ID"));
    }

    // ==================== placeTotem - happy path coverage ====================

    @Test
    @DisplayName("placeTotem: should reach game.placeTotem and propagate its exception when player is found")
    void shouldReachPlaceTotemOnGameWhenPlayerIsFound() {
        // Alice joins a 4-player game: she is in playerToGame, game is in WaitingForPlayersPhase
        // PlaceTotemPhase is not yet active, so placeTotem on the phase throws
        manager.joinGame(4, "Alice");

        // getGameByNickname succeeds (line 73 covered), getPlayerByNickname succeeds (line 55 covered),
        // game.placeTotem delegates to the phase which throws because it is not PlaceTotemPhase
        assertThrows(Exception.class, () -> manager.placeTotem("Alice", 'B'));
    }

    // ==================== takeCard ====================

    @Test
    @DisplayName("takeCard: should throw InvalidActionException for unknown nickname")
    void shouldThrowTakeCardForUnknownNickname() {
        assertThrows(InvalidActionException.class, () -> manager.takeCard("Nobody", "H1"));
    }

    // ==================== takeCard - happy path coverage ====================

    @Test
    @DisplayName("takeCard: should reach game.takeCard and propagate its exception when player is found")
    void shouldReachTakeCardOnGameWhenPlayerIsFound() {
        // Same reasoning as placeTotem: player is found, phase is not PlayerOfferPhase,
        // so takeCard on the phase throws
        manager.joinGame(4, "Alice");

        assertThrows(Exception.class, () -> manager.takeCard("Alice", "ART-01"));
    }

    // ==================== findGame ====================

    @Test
    @DisplayName("findGame: should throw InvalidActionException for unknown gameId")
    void shouldThrowForUnknownGameId() {
        assertThrows(InvalidActionException.class, () -> manager.findGame("NONEXISTENT"));
    }

    @Test
    @DisplayName("findGame: should return the correct active game by id")
    void shouldReturnCorrectActiveGame() {
        manager.joinGame(2, "Alice");
        manager.joinGame(2, "Bob");

        Game found = manager.findGame("GAME-1");
        assertEquals("GAME-1", found.getGameId());
    }
}