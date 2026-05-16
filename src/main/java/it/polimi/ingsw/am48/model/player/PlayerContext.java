package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.snapshot.PlayerContextSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PlayerContext {
    private Player currPlayer;
    private List<Player> players;

    public PlayerContext() {
        players = new ArrayList<>();
    }

    public PlayerContextSnapshot toSnapshot() {
        return new PlayerContextSnapshot(
                players.stream()
                        .map(Player::toSnapshot)
                        .collect(Collectors.toList()),
                currPlayer != null ? currPlayer.getNickname() : null
        );
    }

    public static PlayerContext fromSnapshot(PlayerContextSnapshot snap, Map<String, Card> cardMap) {
        PlayerContext ctx = new PlayerContext();
        ctx.players = snap.getPlayers().stream()
                .map(ps -> Player.fromSnapshot(ps, cardMap))
                .collect(Collectors.toList());

        if (snap.getCurrPlayerNickname() != null) {
            ctx.currPlayer = ctx.players.stream()
                    .filter(p -> p.getNickname().equals(snap.getCurrPlayerNickname()))
                    .findFirst()
                    .orElseThrow();
        }

        return ctx;
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
