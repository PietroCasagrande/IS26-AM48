package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.snapshot.PlayerContextSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Holds the set of players taking part in a game together with the one currently acting.
 * <p>
 * A {@code PlayerContext} is the entry point that card strategies receive when their effect
 * runs: it exposes the {@link #getCurrPlayer() current player} (the target of immediate,
 * single-player effects) and the {@link #getPlayers() full list of players} (the target of
 * event effects that touch everyone).
 *
 * @see Player
 */
public class PlayerContext {
    private Player currPlayer;
    private List<Player> players;

    /**
     * Creates an empty context with no players and no current player set.
     */
    public PlayerContext() {
        players = new ArrayList<>();
    }

    /**
     * Captures the context into an immutable snapshot.
     *
     * @return a {@link PlayerContextSnapshot} holding a snapshot of every player and the
     *         nickname of the current player (or {@code null} if none is set)
     */
    public PlayerContextSnapshot toSnapshot() {
        return new PlayerContextSnapshot(
                players.stream()
                        .map(Player::toSnapshot)
                        .collect(Collectors.toList()),
                currPlayer != null ? currPlayer.getNickname() : null
        );
    }

    /**
     * Rebuilds a context from a snapshot, restoring every player and re-linking the current
     * one by nickname.
     *
     * @param snap    the snapshot describing the persisted context
     * @param cardMap a lookup from card id to the corresponding {@link Card} instance, used
     *                to restore each player's tribe
     * @return the reconstructed {@link PlayerContext}
     * @throws java.util.NoSuchElementException if the snapshot names a current player that is
     *                                          not among the restored players
     */
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
