package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.factory.BoardBuilder;
import it.polimi.ingsw.am48.model.factory.CardMapBuilder;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.phase.WaitingForPlayersPhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Aggregate root representing a single game session and all of its mutable state.
 *
 * <p>A {@code Game} bundles together the participating players ({@link PlayerContext}), the
 * shared {@link Board}, the {@link NotificatorCenter} driving card-effect triggers, the
 * current turn counter and the active {@link GamePhase}. It applies the State pattern: the
 * player actions ({@link #addPlayer}, {@link #placeTotem}, {@link #takeCard}) are not
 * implemented here but delegated to the current phase, which validates them and decides the
 * resulting state transitions. Higher-level coordination across sessions (lobbies,
 * persistence, reconnection) is handled by {@link GameManager}.
 *
 * <p>The game can be serialised to a {@link GameSnapshot} and rebuilt from one, enabling the
 * crash-recovery flow orchestrated by {@link GameManager}.
 *
 * @see GameManager
 * @see GamePhase
 * @see GameSnapshot
 */
public class Game {
    private final String gameId;
    private final int numPlayers;
    private PlayerContext playerContext;
    private Board board;
    private final NotificatorCenter notificatorCenter;
    private GamePhase currentPhase;
    private int currentTurn;
    private Set<String> reconnectedPlayers;

    /**
     * Creates a new, empty game starting in {@link WaitingForPlayersPhase} on turn 1.
     *
     * @param gameId     the unique identifier of the game (e.g. {@code "GAME-1"})
     * @param numPlayers the number of players the game will accommodate
     */
    public Game(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.playerContext = new PlayerContext();
        this.notificatorCenter = new NotificatorCenter();
        this.currentTurn = 1;
        this.currentPhase = new WaitingForPlayersPhase(numPlayers);
        this.reconnectedPlayers = new HashSet<>();
    }

    // Methods exposed to the controller

    /**
     * Adds a player with the given nickname to the game, delegating to the current phase.
     *
     * @param nickname the nickname of the joining player
     */
    public void addPlayer(String nickname) {
        currentPhase.addPlayer( playerContext,this, nickname);
    }

    /**
     * Performs a totem-placement move, delegating validation and resolution to the current phase.
     *
     * @param player   the player placing the totem
     * @param position the offer-track slot on which the totem is placed
     * @return the list of {@link GameDelta} objects produced by the move
     */
    public List<GameDelta> placeTotem(Player player, char position) {
        return currentPhase.placeTotem(this, player, position);
    }

    /**
     * Performs a card-take move, delegating validation and resolution to the current phase.
     *
     * @param player the player taking the card
     * @param cardId the identifier of the card to take
     * @return the list of {@link GameDelta} objects produced by the move
     */
    public List<GameDelta> takeCard(Player player, String cardId) {
        return currentPhase.takeCard(this, player, cardId);
    }

    /**
     * Looks up a participating player by nickname.
     *
     * @param nickname the nickname to search for
     * @return the matching {@link Player}
     * @throws InvalidActionException if no player with that nickname is in the game
     */
    // Player lookup, needed by GameManager's methods
    public Player getPlayerByNickname(String nickname){
        return playerContext.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new InvalidActionException("Player not found: " + nickname));
    }

    // getters
    public Board getBoard() { return board; }
    public String getGameId() { return gameId; }
    public int getNumPlayers() { return numPlayers; }
    public PlayerContext getPlayerContext() { return playerContext; }
    public NotificatorCenter getNotificatorCenter() { return notificatorCenter; }
    public GamePhase getCurrentPhase() { return currentPhase; }
    public int getCurrentTurn() { return currentTurn; }

    /**
     * Returns whether the game has reached its full roster of players.
     *
     * @return {@code true} if the number of joined players equals {@code numPlayers}
     */
    public boolean isFull() { return playerContext.getPlayers().size() == numPlayers; }

    // End game logic: checkEnd() and computeEndGameScore() should already be managed by GamePhase
    // public boolean checkEnd(){ throw new UnsupportedOperationException("TODO"); }
    // public GameDelta computeEndGameScore(List<Player> players){ throw new UnsupportedOperationException("TODO"); }
    /**
     * Determines the winner as the player holding the most prestige points.
     *
     * @return the {@link Player} with the highest score
     * @throws InvalidActionException if there are no players in the game
     */
    public Player findWinner(){
        return playerContext.getPlayers().stream()
                .max((p1,p2) -> Integer.compare(p1.getPoints(), p2.getPoints()))
                .orElseThrow(() -> new InvalidActionException("No players in the game."));
    }

    /**
     * Marks the given player as having reconnected after a server crash.
     *
     * @param nickname the nickname of the reconnected player
     */
    // marks the player as reconnected
    public void markReconnected(String nickname) {
        reconnectedPlayers.add(nickname);
    }

    /**
     * Returns whether every player of this game has reconnected.
     *
     * @return {@code true} if all participating players are marked as reconnected
     */
    // true if all the players of the game have reconnected
    public boolean allPlayersReconnected() {
        return playerContext.getPlayers().stream()
                .map(Player::getNickname)
                .allMatch(reconnectedPlayers::contains);
    }

    // Package-private: only Phases set phases
    public void setPhase(GamePhase phase) { this.currentPhase = phase; }
    public void setBoard(Board board) { this.board = board; }
    public void incrementTurn() { this.currentTurn++; }

    /**
     * Captures the full state of the game into an immutable snapshot.
     * <p>
     * The board is included only once it exists; during {@link WaitingForPlayersPhase} the
     * board has not been created yet, so a {@code null} board is stored instead.
     *
     * @return a {@link GameSnapshot} describing the current game state
     */
    public GameSnapshot toSnapshot(){
        List<PlayerSnapshot> playerSnapshots = playerContext.getPlayers().stream()
                .map(Player::toSnapshot)
                .toList();

        return new GameSnapshot(
                gameId,
                numPlayers,
                playerContext.toSnapshot(),
                currentTurn,
                // board!=null is necessary because during WaitingForPlayersPhase the board has not been created yet
                board != null ? board.toSnapshot() : null,
                currentPhase.toSnapshot()
        );
    }

    /**
     * Rebuilds a complete game from a snapshot, restoring players, board, phase and turn.
     * <p>
     * After reconstructing the state, the strategies of every player's buildings are
     * re-registered on the {@link NotificatorCenter}: building effects listen for triggers,
     * so without re-registration they would never fire again after a reload (character
     * effects are immediate and need no registration).
     *
     * @param snapshot the snapshot describing the persisted game state
     * @return the reconstructed {@link Game}
     */
    public static Game fromSnapshot(GameSnapshot snapshot){
        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(snapshot.getNumPlayers());
        Game game = new Game(snapshot.getGameId(), snapshot.getNumPlayers());

        game.playerContext = PlayerContext.fromSnapshot(snapshot.getPlayerContext(), cardMap);
        List<Player> players = game.playerContext.getPlayers();

        game.board = Board.fromSnapshot(snapshot.getBoard(), cardMap,  players);

        game.currentPhase = GamePhase.fromSnapshot(snapshot.getPhase(), players, game.numPlayers);

        game.currentTurn = snapshot.getCurrentTurn();

        // re-register the building strategies of all players, otherwise they would never be notified (characters do not register)
        game.playerContext.getPlayers().forEach(p -> {
            p.getTribe().getBuildings().forEach(b -> b.getStrategy().registerTo(game.notificatorCenter, game.playerContext));
        });

        return game;
    }
}
