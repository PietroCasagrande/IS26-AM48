package it.polimi.ingsw.am48.repository;

import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    void save(String gameId, GameSnapshot gameSnapshot);
    Optional<GameSnapshot> load(String gameId);
    void delete(String gameId);
    List<String> listActiveGameIds();
}
