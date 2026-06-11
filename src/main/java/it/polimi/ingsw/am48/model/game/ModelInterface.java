package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;

/**
 * Contract exposed by the model to the network layer (Socket / RMI handlers).
 *
 * <p>{@code ModelInterface} is the single boundary through which the server drives the game
 * logic: it groups the lobby operations (joining a game), the in-game player actions
 * (placing a totem, taking a card) and the queries needed to keep clients informed
 * (snapshots, game state, disconnection handling). The reference implementation is
 * {@link GameManager}, which coordinates all concurrent game sessions.
 *
 * @see GameManager
 * @see Game
 */
public interface ModelInterface {
    // metodi lobby: gestione partite

    /**
     * Handles a player's request to join (or reconnect to) a game of the given size.
     *
     * @param numPlayers the desired game size (typically between 2 and 5 inclusive)
     * @param nickname   the player's chosen display name
     * @return a {@link JoinResult} carrying the current game snapshot and whether the game has started
     */
    JoinResult joinGame(int numPlayers, String nickname);

    // metodi in-game: azioni del giocatore

    /**
     * Processes a totem-placement move for the given player.
     *
     * @param nickname the nickname of the player placing the totem
     * @param position the offer-track slot on which the totem is placed
     * @return the list of {@link GameDelta} objects produced by the move, to be forwarded to clients
     */
    List<GameDelta> placeTotem(String nickname, char position);

    /**
     * Processes a card-take move for the given player.
     *
     * @param name   the nickname of the player taking the card
     * @param cardId the identifier of the card to take
     * @return the list of {@link GameDelta} objects produced by the move; when the move ends
     *         the game, the list may also include the final leaderboard update
     */
    List<GameDelta> takeCard(String name, String cardId);

    /**
     * Returns a fresh snapshot of the game the given player is currently in.
     *
     * @param nickname the player's nickname
     * @return the current {@link GameSnapshot}
     */
    GameSnapshot getSnapshotForNickname(String nickname);

    /**
     * Returns whether the game the given player belongs to has reached its maximum size.
     *
     * @param nickname the player's nickname
     * @return {@code true} if the game is full, {@code false} otherwise
     */
    boolean isGameFull(String nickname);

    /**
     * Returns the nicknames of all players in the same game as the given player.
     *
     * @param nickname the nickname of any player in the target game
     * @return the list of nicknames in that game
     */
    List<String> getPlayersInGame(String nickname);

    /**
     * Cleans up the server-side state of a disconnecting client's game.
     *
     * @param nickname the nickname of the disconnecting player
     * @return the nicknames of the other players in the same game, so they can be notified;
     *         empty if the player had not joined any game
     */
    List<String> handleClientDisconnect(String nickname);
}
