package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.model.board.Board;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardBuilderTest {

    private BoardBuilder boardBuilder;

    @BeforeEach
    void setUp() {
        boardBuilder = new BoardBuilder();
    }

    // ==================== createBoard ====================

    @Test
    @DisplayName("createBoard: should return a non-null Board for 2 players")
    void shouldReturnNonNullBoardForTwoPlayers() {
        Board board = boardBuilder.createBoard(2);
        assertNotNull(board);
    }

    @Test
    @DisplayName("createBoard: should return a non-null Board for 3 players")
    void shouldReturnNonNullBoardForThreePlayers() {
        Board board = boardBuilder.createBoard(3);
        assertNotNull(board);
    }

    @Test
    @DisplayName("createBoard: should return a non-null Board for 4 players")
    void shouldReturnNonNullBoardForFourPlayers() {
        Board board = boardBuilder.createBoard(4);
        assertNotNull(board);
    }

    @Test
    @DisplayName("createBoard: should return a non-null Board for 5 players")
    void shouldReturnNonNullBoardForFivePlayers() {
        Board board = boardBuilder.createBoard(5);
        assertNotNull(board);
    }

    @Test
    @DisplayName("createBoard: should initialize tribeShowed with cards drawn from the deck")
    void shouldInitializeTribeShowedWithCards() {
        Board board = boardBuilder.createBoard(2);
        assertNotNull(board.getTribeShowed());
    }

    @Test
    @DisplayName("createBoard: should initialize buildingShowed correctly")
    void shouldInitializeBuildingShowedCorrectly() {
        Board board = boardBuilder.createBoard(2);
        assertNotNull(board.getBuildingShowed());
    }

    @Test
    @DisplayName("createBoard: should set placeOrder correctly via OfferTurnCard")
    void shouldSetPlaceOrderCorrectly() {
        Board board = boardBuilder.createBoard(2);
        assertNotNull(board.getPlaceOrder());
        assertTrue(board.getPlaceOrder().isEmpty());
    }

    @Test
    @DisplayName("createBoard: should return different Board instances on repeated calls")
    void shouldReturnDistinctBoardInstances() {
        Board board1 = boardBuilder.createBoard(2);
        Board board2 = boardBuilder.createBoard(2);
        assertNotSame(board1, board2);
    }
}