package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.model.game.GameResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class GameResultTest {

    @Test
    @DisplayName("constructor and getters: should correctly store and retrieve game result data")
    void shouldStoreAndRetrieveCorrectData() {
        // Arrange
        String gameId = "game-123";
        String nickname = "PlayerOne";
        int score = 42;
        int players = 4;
        LocalDateTime now = LocalDateTime.now();

        GameResult result = new GameResult(gameId, nickname, score, players, now);

        assertAll("GameResult fields",
            () -> assertEquals(gameId, result.getGameId(), "Game ID should match"),
            () -> assertEquals(nickname, result.getPlayerNickname(), "Nickname should match"),
            () -> assertEquals(score, result.getFinalScore(), "Final score should match"),
            () -> assertEquals(players, result.getNumPlayers(), "Number of players should match"),
            () -> assertEquals(now, result.getGameDate(), "Game date should match")
        );
    }
}