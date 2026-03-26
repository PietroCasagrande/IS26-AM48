package it.polimi.ingsw.am48.repository;

import it.polimi.ingsw.am48.model.game.GameResult;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    void save(String gameId, Object gameSnapshot);  // Object is GameSnapshot
    Optional<GameResult> load(String gameId);
    void delete(String gameId);
    boolean exists(String gameId);
    List<String> listActiveGameIds();
}
