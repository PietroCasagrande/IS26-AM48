package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.util.List;

public class LeaderboardDelta extends GameDelta{
    private final List<GameResult> leaderboard;

    @JsonCreator
    public LeaderboardDelta(@JsonProperty("leaderboard") List<GameResult> leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public void applyTo(ClientModel model){
        model.setLeaderboard(leaderboard);
    }
}
