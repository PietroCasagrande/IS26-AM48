package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class PlayerContextSnapshot implements Serializable {
    List<PlayerSnapshot> players;
    String currPlayerNickname;

    @JsonCreator
    public PlayerContextSnapshot(
            @JsonProperty("players") List<PlayerSnapshot> players,
            @JsonProperty("currPlayerNickname") String currPlayerNickname) {
        this.players = players;
        this.currPlayerNickname = currPlayerNickname;
    }

    public List<PlayerSnapshot> getPlayers() {
        return players;
    }

    public String getCurrPlayerNickname() {
        return currPlayerNickname;
    }
}
