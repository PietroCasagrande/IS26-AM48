package it.polimi.ingsw.am48.repository;

import it.polimi.ingsw.am48.model.game.GameResult;

import java.util.List;

/**
 * Data-access abstraction for persisting and querying game results.
 *
 * <p>Implementations of this interface are responsible for storing each player's final score
 * at the end of a game and for retrieving a ranked list of results filtered by player count.
 * Callers are fully decoupled from the underlying storage technology through this interface.
 *
 * @see MySqlLeaderboardRepository
 */
public interface LeaderboardRepository {

    /**
     * Persists the result of a single player at the end of a completed game.
     *
     * @param result the game result to save; must not be {@code null}
     * @throws RuntimeException if the underlying storage operation fails
     */
    void saveResult(GameResult result);

    /**
     * Returns all game results for games played with the given number of players,
     * ordered by {@code finalScore} descending.
     *
     * @param numPlayers the player count to filter by (e.g. {@code 2}, {@code 3}, or {@code 4})
     * @return an ordered list of {@link GameResult} entries; never {@code null}, may be empty
     * @throws RuntimeException if the underlying query fails
     */
    List<GameResult> getLeaderboard(int numPlayers);
}
