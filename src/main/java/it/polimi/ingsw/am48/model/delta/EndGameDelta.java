package it.polimi.ingsw.am48.model.delta;

import java.util.Map;

public class EndGameDelta extends GameDelta{
    private final Map<String, Integer> finalScores;     // nickname -> final score
    private final String winnerNickname;

    public EndGameDelta(Map<String, Integer> finalScores, String winnerNickname) {
        this.finalScores = finalScores;
        this.winnerNickname = winnerNickname;
    }

    public Map<String, Integer> getFinalScores() {
        return finalScores;
    }

    public String getWinnerNickname() {
        return winnerNickname;
    }
}
