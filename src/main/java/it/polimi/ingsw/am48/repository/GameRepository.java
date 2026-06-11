package it.polimi.ingsw.am48.repository;

import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the persistent storage of game snapshots.
 * <p>
 * A repository associates each game with a unique identifier and provides the basic
 * operations to persist, retrieve, remove and enumerate saved games, decoupling the rest
 * of the model from the concrete storage technology. Implementations (such as
 * {@link JsonGameRepository}) decide where and how the {@link GameSnapshot}s are actually
 * stored.
 *
 * @see GameSnapshot
 * @see JsonGameRepository
 */
public interface GameRepository {
    /**
     * Persists the given snapshot under the specified game id, overwriting any snapshot
     * already stored for the same id.
     *
     * @param gameId       the unique identifier of the game
     * @param gameSnapshot the snapshot capturing the game state to persist
     */
    void save(String gameId, GameSnapshot gameSnapshot);

    /**
     * Retrieves the persisted snapshot for the given game id.
     *
     * @param gameId the unique identifier of the game to load
     * @return an {@link Optional} containing the stored {@link GameSnapshot}, or an empty
     *         optional if no game is stored under that id
     */
    Optional<GameSnapshot> load(String gameId);

    /**
     * Removes the persisted game with the given id, if present.
     *
     * @param gameId the unique identifier of the game to delete
     */
    void delete(String gameId);

    /**
     * Lists the identifiers of all currently stored (active) games.
     *
     * @return a list of the game ids that have a persisted snapshot
     */
    List<String> listActiveGameIds();
}
