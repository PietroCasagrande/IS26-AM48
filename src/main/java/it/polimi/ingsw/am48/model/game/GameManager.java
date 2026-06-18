package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.LeaderboardDelta;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central coordinator for all active game sessions running on the Mesos server.
 *
 * <p>{@code GameManager} implements {@link ModelInterface} and is the single entry point
 * through which the network layer (Socket / RMI handlers) interacts with the game logic.
 * It maintains four internal registries:
 * <ul>
 *   <li>{@code activeGames} — games currently in progress, keyed by game ID.</li>
 *   <li>{@code waitingGames} — one lobby per player count, waiting for more players to join.</li>
 *   <li>{@code crashedGames} — games loaded from persistent storage after a server crash,
 *       awaiting player reconnection before resuming.</li>
 *   <li>{@code playerToGame} — reverse index from nickname to {@link Game}, used by all
 *       move methods to locate the correct game instance in O(1).</li>
 * </ul>
 *
 * <p><b>Thread safety:</b> public methods synchronise on {@code this} for mutations to the
 * shared registries and on the individual {@link Game} instance for move execution. This
 * two-level locking strategy allows concurrent play in different games while serialising
 * operations within the same game.
 *
 * <p><b>Advanced features:</b>
 * <ul>
 *   <li>When a non-null {@link GameRepository} is provided, every state change is persisted
 *       as a snapshot to support crash recovery via {@link #loadCrashedGames()}.</li>
 *   <li>When a non-null {@link LeaderboardRepository} is provided, final scores are written
 *       to the database at game end and the updated leaderboard is appended to the delta
 *       list returned by {@link #takeCard}.</li>
 * </ul>
 */
public class GameManager implements ModelInterface{
    private final Map<String, Game> activeGames;
    private final Map<Integer, Game> waitingGames;
    private final Map<String, Game> crashedGames;
    private final Map<String, Game> playerToGame;
    private int nextGameId;

    // repository for advanced features
    private final GameRepository gameRepository;
    private final LeaderboardRepository leaderboardRepository;

    /**
     * No-arg constructor that initialises {@code GameManager} without repository support.
     * Intended for unit tests that exercise game logic without a database or filesystem.
     */
    public GameManager() {
        this(null, null);
    }

    /**
     * Constructs a fully-featured {@code GameManager} with optional repository support.
     *
     * <p>{@code nextGameId} is initialised by scanning any snapshot files already on disk,
     * preventing game-ID collisions after a server restart from overwriting existing snapshots.
     *
     * @param gameRepository        the persistence backend for game snapshots; may be {@code null}
     *                              to disable crash-recovery support
     * @param leaderboardRepository the persistence backend for game results; may be {@code null}
     *                              to disable leaderboard support
     */
    public GameManager(GameRepository gameRepository, LeaderboardRepository leaderboardRepository){
        this.activeGames = new HashMap<>();
        this.waitingGames = new HashMap<>();
        this.crashedGames = new HashMap<>();
        this.playerToGame = new HashMap<>();

        this.gameRepository = gameRepository;
        this.leaderboardRepository = leaderboardRepository;

        this.nextGameId = computeNextGameId();
    }

    // =========================================================================
    // ModelInterface implementation
    // =========================================================================

    /**
     * Handles a player's request to join a game with the given number of players.
     *
     * <p>The method covers three scenarios in order:
     * <ol>
     *   <li><b>Reconnect:</b> if the nickname is already mapped to a crashed game, the player
     *       is reconnected and the current snapshot is returned immediately.</li>
     *   <li><b>Stale entry:</b> if the nickname is mapped to a game that has already ended
     *       (turn &gt; 10), the stale mapping is cleaned up and the player is treated as new.</li>
     *   <li><b>New join:</b> the player is added to an existing waiting lobby for the given
     *       {@code numPlayers}, or a new lobby is created. Once the lobby is full, the game
     *       starts: the initial snapshot is saved and the returned {@link JoinResult} carries
     *       {@code started = true}.</li>
     * </ol>
     *
     * @param numPlayers the desired game size (must be between 2 and 5 inclusive)
     * @param nickname   the player's chosen display name
     * @return a {@link JoinResult} containing the current game snapshot and whether the game has started
     * @throws InvalidActionException   if the nickname is already in use in an active game
     * @throws IllegalArgumentException if {@code numPlayers} is outside the range [2, 5]
     */
    @Override
    public JoinResult joinGame(int numPlayers, String nickname) {
        synchronized (this) {
            if (playerToGame.containsKey(nickname)) {
                Game game = playerToGame.get(nickname);

                if (crashedGames.containsValue(game)) {
                    return handleReconnect(nickname, game);
                }

                if (game.getCurrentTurn() > 10) {
                    activeGames.remove(game.getGameId());
                    game.getPlayerContext().getPlayers().forEach(p -> {
                        playerToGame.remove(p.getNickname());
                    });
                } else {
                    throw new InvalidActionException("Nickname già in uso: " + nickname);
                }
            }
        }

        synchronized (this) {
            if(numPlayers < 2 || numPlayers > 5) {
                throw new IllegalArgumentException("Invalid number of players");
            }
            if(playerToGame.containsKey(nickname)){
                throw new InvalidActionException("Nickname già in uso: " + nickname);
            }
        }

        Game game;
        synchronized (this) {
            game = findAvailableGame(numPlayers).orElseGet(() -> createGame(numPlayers));
        }

        synchronized (game) {
            game.addPlayer(nickname);
            synchronized (this) { playerToGame.put(nickname, game); }
            boolean started = game.isFull();
            if (started) {
                synchronized (this) {
                    waitingGames.remove(numPlayers);
                    activeGames.put(game.getGameId(), game);
                }
                if(gameRepository != null) {
                    gameRepository.save(game.getGameId(), game.toSnapshot());
                }
            }

            GameSnapshot snapshot = game.toSnapshot();
            JoinResult result = new JoinResult(snapshot, started);
            return result;
        }
    }

    /**
     * Processes a totem-placement move for the given player.
     *
     * <p>Delegates to {@link Game#placeTotem} under the game's monitor lock and immediately
     * persists the updated snapshot via {@link GameRepository#save}.
     *
     * @param nickname the nickname of the player placing the totem
     * @param position the offer-track slot (A–G) on which the totem is placed
     * @return the list of {@link GameDelta} objects produced by the move, to be forwarded to clients
     * @throws InvalidActionException if the move violates game rules
     */
    @Override
    public List<GameDelta> placeTotem(String nickname, char position){
        Game game = getGameByNickname(nickname);
        synchronized (game) {
            Player player = game.getPlayerByNickname(nickname);
            List<GameDelta> deltas = game.placeTotem(player, position);
            gameRepository.save(game.getGameId(), game.toSnapshot());
            return deltas;
        }
    }

    /**
     * Processes a card-take move for the given player and handles end-of-game logic.
     *
     * <p>After delegating to {@link Game#takeCard}, checks whether the game has ended
     * (turn &gt; 10). If so:
     * <ol>
     *   <li>Each player's final score is persisted to the {@link LeaderboardRepository}
     *       with a shared {@code game_date} timestamp.</li>
     *   <li>The updated leaderboard for the same player count is fetched and appended as a
     *       {@link LeaderboardDelta} to the returned list, so clients receive it in the same
     *       network message as the final game state.</li>
     *   <li>The game snapshot is deleted from the {@link GameRepository} since crash-recovery
     *       is no longer needed once the game has concluded.</li>
     * </ol>
     * For non-final moves, only the updated snapshot is persisted.
     *
     * @param nickname the nickname of the player taking the card
     * @param cardId   the identifier of the card to take
     * @return the list of {@link GameDelta} objects produced by the move; if the game ended,
     *         the list includes a trailing {@link LeaderboardDelta}
     * @throws InvalidActionException if the move violates game rules
     */
    @Override
    public List<GameDelta> takeCard(String nickname, String cardId){
        Game game = getGameByNickname(nickname);
        synchronized (game) {
            Player player = game.getPlayerByNickname(nickname);
            List<GameDelta> deltas = game.takeCard(player, cardId);
            gameRepository.save(game.getGameId(), game.toSnapshot());

            if(game.getCurrentTurn() > 10){
                LocalDateTime now = LocalDateTime.now();

                game.getPlayerContext().getPlayers().forEach(p -> {
                    leaderboardRepository.saveResult(new GameResult(
                            game.getGameId(),
                            p.getNickname(),
                            p.getPoints(),
                            game.getNumPlayers(),
                            now
                    ));
                });

                List<GameResult> leaderboard = leaderboardRepository.getLeaderboard(game.getNumPlayers());
                deltas.add(new LeaderboardDelta(leaderboard));
                gameRepository.delete(game.getGameId());
            }

            return deltas;
        }
    }

    // =========================================================================
    // Game lookup
    // =========================================================================

    /**
     * Returns the {@link Game} instance that the given player is currently participating in.
     *
     * @param nickname the player's nickname
     * @return the corresponding {@link Game}
     * @throws InvalidActionException if no game is found for the given nickname
     */
    public Game getGameByNickname(String nickname){
        Game game = playerToGame.get(nickname);
        if(game == null){
            throw new InvalidActionException("Nessuna partita trovata per: " + nickname);
        }
        return game;
    }

    /**
     * Returns the waiting lobby for the given player count, if one exists.
     *
     * @param numPlayers the desired game size
     * @return an {@link Optional} containing the waiting {@link Game}, or empty if none exists
     */
    private Optional<Game> findAvailableGame(int numPlayers){
        return Optional.ofNullable(waitingGames.get(numPlayers));
    }

    /**
     * Creates a new {@link Game} with a fresh ID, registers it in {@code waitingGames},
     * and returns it.
     *
     * @param numPlayers the number of players the new game will accommodate
     * @return the newly created {@link Game}
     */
    private Game createGame(int numPlayers){
        String gameId = "GAME-" + nextGameId++;
        Game game = new Game(gameId, numPlayers);
        waitingGames.put(numPlayers, game);
        return game;
    }

    /**
     * Handles a reconnection request for a player whose game is in {@code crashedGames}.
     *
     * <p>Marks the player as reconnected on the game. If all players in that game have
     * now reconnected, the game is moved from {@code crashedGames} back to {@code activeGames}.
     * The current snapshot is always returned so the reconnecting client can resume from
     * the correct state, regardless of whether others have reconnected yet.
     *
     * @param nickname the nickname of the reconnecting player
     * @param game     the crashed game the player belongs to
     * @return a {@link JoinResult} containing the current snapshot and whether all players
     *         in the game have now reconnected
     */
    private JoinResult handleReconnect(String nickname, Game game) {
        synchronized (game) {
            game.markReconnected(nickname); // segna il giocatore come riconnesso

            boolean allReconnected = game.allPlayersReconnected();
            if (allReconnected) {
                synchronized (this) {
                    crashedGames.remove(game.getGameId());
                    activeGames.put(game.getGameId(), game);
                }
            }

            // always sends the current snapshot — whether or not all players have reconnected
            return new JoinResult(game.toSnapshot(), allReconnected);
        }
    }

    // =========================================================================
    // Persistence and crash recovery
    // =========================================================================

    /**
     * Loads all game snapshots persisted by the previous server run and registers them as
     * crashed games awaiting player reconnection.
     *
     * <p>Should be called once at server startup, before accepting any client connections.
     * For each snapshot found, the corresponding {@link Game} is reconstructed via
     * {@link Game#fromSnapshot} and every player in it is added to the {@code playerToGame}
     * reverse index, so that incoming {@code join} calls will trigger the reconnect flow
     * rather than creating a new game.
     */
    public void loadCrashedGames() {
        for (String gameId : gameRepository.listActiveGameIds()) {
            gameRepository.load(gameId).ifPresent(snapshot -> {
                Game game = Game.fromSnapshot(snapshot);
                crashedGames.put(gameId, game);
                // popola anche playerToGame così getGameByNickname funziona
                game.getPlayerContext().getPlayers().forEach(p ->
                        playerToGame.put(p.getNickname(), game)
                );
            });
        }
    }

    /**
     * Computes the next available game ID by scanning the IDs of existing snapshot files.
     *
     * <p>At server restart, already-assigned IDs must be skipped to avoid overwriting
     * persisted snapshots of crashed games. Returns {@code 1} if no snapshots exist or if
     * {@code gameRepository} is {@code null}.
     *
     * @return the next integer to use when formatting a game ID (e.g. {@code "GAME-3"})
     */
    private int computeNextGameId(){
        if(gameRepository == null) return 1;
        return gameRepository.listActiveGameIds().stream()
                .map(id -> id.replace("GAME-", ""))
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0) + 1;
    }

    // =========================================================================
    // Disconnection handling
    // =========================================================================

    /**
     * Cleans up all server-side state associated with a disconnecting client's game.
     *
     * <p>Removes the game from all registries ({@code activeGames}, {@code waitingGames},
     * {@code crashedGames}) and evicts <em>every</em> player in that game from
     * {@code playerToGame} — not only the disconnecting one — so no stale nickname
     * mappings remain for the companions. The game snapshot is also deleted from the
     * {@link GameRepository}. The returned list of companion nicknames is used by the
     * network layer to notify the remaining players that their game has been terminated.
     *
     * @param nickname the nickname of the disconnecting player
     * @return the nicknames of the other players in the same game; empty if the player
     *         had not yet joined any game
     */
    @Override
    public List<String> handleClientDisconnect(String nickname) {
        synchronized (this) {
            Game game = playerToGame.get(nickname);
            if(game == null) return List.of();

            // get players before throwing down everything about the game
            List<String> companions = game.getPlayerContext().getPlayers().stream()
                    .map(Player::getNickname)
                    .filter(n -> !n.equals(nickname))
                    .toList();

            String gameId = game.getGameId();

            // frees every game's nickname (not only the disconnected one)
            // otherwise, companions would still be mapped to a game that doesn't belong to activeGames anymore
            game.getPlayerContext().getPlayers().forEach(p -> playerToGame.remove(p.getNickname()));

            activeGames.remove(gameId);
            waitingGames.values().remove(game);
            crashedGames.remove(gameId);

            if (gameRepository != null) gameRepository.delete(gameId);

            return companions;
        }
    }

    // =========================================================================
    // ModelInterface — additional queries
    // =========================================================================

    /**
     * Returns the active {@link Game} with the given ID.
     *
     * @param gameId the game identifier (e.g. {@code "GAME-1"})
     * @return the corresponding {@link Game}
     * @throws InvalidActionException if no active game with the given ID is found
     */
    public Game findGame(String gameId){
        Game game = activeGames.get(gameId);
        if(game == null){
            throw new InvalidActionException("Partita non trovata: " + gameId);
        }
        return game;
    }

    /**
     * Returns a fresh snapshot of the game that the given player is currently in.
     *
     * @param nickname the player's nickname
     * @return the current {@link GameSnapshot}
     */
    @Override
    public GameSnapshot getSnapshotForNickname(String nickname) {
        return getGameByNickname(nickname).toSnapshot();
    }

    /**
     * Returns whether the game that the given player belongs to has reached its maximum
     * player count and has started.
     *
     * @param nickname the player's nickname
     * @return {@code true} if the game is full; {@code false} otherwise
     */
    @Override
    public boolean isGameFull(String nickname) {
        return getGameByNickname(nickname).isFull();
    }

    /**
     * Returns the nicknames of all players currently in the same game as the given player.
     *
     * @param nickname the nickname of any player in the target game
     * @return an unmodifiable list of all nicknames in that game
     */
    public List<String> getPlayersInGame(String nickname){
        Game game = getGameByNickname(nickname);
        return game.getPlayerContext().getPlayers()
                .stream()
                .map(Player::getNickname)
                .toList();
    }
}
