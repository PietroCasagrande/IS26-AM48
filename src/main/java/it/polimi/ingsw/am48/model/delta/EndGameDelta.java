package it.polimi.ingsw.am48.model.delta;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EndGameDelta extends GameDelta{
    private final Map<String, Integer> finalScores;     // nickname -> final score
    private final String winnerNickname;

    @JsonCreator
    public EndGameDelta(
            @JsonProperty("finalScores") Map<String, Integer> finalScores,
            @JsonProperty("winnerNickname") String winnerNickname) {
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
