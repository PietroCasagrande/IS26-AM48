package it.polimi.ingsw.am48.repository;

import it.polimi.ingsw.am48.model.game.GameResult;

import java.util.List;

public interface LeaderboardRepository {
    void saveResult(GameResult result);
    List<GameResult> getLeaderboard(int numPlayers);
}
