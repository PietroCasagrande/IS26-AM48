package it.polimi.ingsw.am48.model.delta;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class EndGameDelta extends GameDelta{
    private final Map<String, Integer> finalScores;     // nickname -> final score
    private final String winnerNickname;
    // attualmente winner MAI UTILIZZATO in ambito di delta,
    // dobbiamo capire se passarlo al ClientModel (aggiungendo un attributo winnerNickname in esso)
    // oppure se toglierlo dal delta perchè relativo a un puro calcolo del model

    @JsonCreator
    public EndGameDelta(
            @JsonProperty("finalScores") Map<String, Integer> finalScores,
            @JsonProperty("winnerNickname") String winnerNickname) {
        this.finalScores = finalScores;
        this.winnerNickname = winnerNickname;
    }

    // EndGame modifica pp di tutti i giocatori
    @Override
    public void applyTo(ClientModel model) {
        finalScores.keySet().forEach(nick -> {
            model.updatePlayerPoints(nick, finalScores.get(nick));
        });
    };

    public Map<String, Integer> getFinalScores() {
        return finalScores;
    }
    public String getWinnerNickname() {
        return winnerNickname;
    }
}
