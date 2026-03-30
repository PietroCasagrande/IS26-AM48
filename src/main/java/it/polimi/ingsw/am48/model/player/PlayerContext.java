package it.polimi.ingsw.am48.model.player;

import java.util.ArrayList;
import java.util.List;

public class PlayerContext {
    private Player currPlayer;
    private List<Player> players;

    public PlayerContext() {
        players = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrPlayer() {
        return currPlayer;
    }

    public void setCurrPlayer(Player currPlayer) {
        this.currPlayer = currPlayer;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
}
