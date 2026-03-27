package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameManager {
    private final Map<String, Game> activeGames = new HashMap<>();
    private final Map<Integer, Game> waitingGames = new HashMap<>();

    private GameRepository gameRepository;
    private LeaderboardRepository leaderboardRepository;

    public void joinGame(int numPlayers, String nickname) { throw new UnsupportedOperationException("TODO"); }
    public Optional<Game> findAvailableGame(int numPlayers) { throw new UnsupportedOperationException("TODO"); }
    public Game findGame(String gameId) { throw new UnsupportedOperationException("TODO"); }
    public GameResult createGameResult(){ throw new UnsupportedOperationException("TODO"); }
    public void SaveGame(String gameId){ throw new UnsupportedOperationException("TODO"); }

    // mancano metodi
}
