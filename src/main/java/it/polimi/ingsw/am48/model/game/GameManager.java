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

public class GameManager implements ModelInterface{
    private final Map<String, Game> activeGames;
    private final Map<Integer, Game> waitingGames;
    private final Map<String, Game> crashedGames;
    private final Map<String, Game> playerToGame;
    private int nextGameId;

    // repository per le funzionalità avanzate
    private final GameRepository gameRepository;
    private final LeaderboardRepository leaderboardRepository;

    // default constructor needed for mvn test to run while implementing additional-features
    public GameManager() {
        this(null, null);
    }

    public GameManager(GameRepository gameRepository, LeaderboardRepository leaderboardRepository){
        this.activeGames = new HashMap<>();
        this.waitingGames = new HashMap<>();
        this.crashedGames = new HashMap<>();
        this.playerToGame = new HashMap<>();
        this.nextGameId = 1;

        this.gameRepository = gameRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    // ModelInterface implementation: joinGame, placeTotem and takeCard methods
    @Override
    public JoinResult joinGame(int numPlayers, String nickname){

        // controlliamo se questa join è una riconnessione post-crash o meno
        synchronized (this) {
            if (playerToGame.containsKey(nickname)) {
                Game game = playerToGame.get(nickname);
                if (crashedGames.containsValue(game)) {
                    return handleReconnect(nickname, game);
                }
                throw new InvalidActionException("Nickname già in uso: " + nickname);
            }
        }
        synchronized (this) {
            if(numPlayers < 2 || numPlayers > 5) {
                throw new IllegalArgumentException("Invalid number of players: must be between 2 and 5");
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
                gameRepository.save(game.getGameId(), game.toSnapshot());
            }
            GameSnapshot snapshot = game.toSnapshot();
            return new JoinResult(snapshot, started);
        }
    }

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

    // Game lookup:
    // metodo getGameByNickname ritorna il game in cui sta giocando il player con nickname passato x parametro
    public Game getGameByNickname(String nickname){
        Game game = playerToGame.get(nickname);
        if(game == null){
            throw new InvalidActionException("Nessuna partita trovata per: " + nickname);
        }
        return game;
    }

    private Optional<Game> findAvailableGame(int numPlayers){
        return Optional.ofNullable(waitingGames.get(numPlayers));
    }

    private Game createGame(int numPlayers){
        String gameId = "GAME-" + nextGameId++;
        Game game = new Game(gameId, numPlayers);
        waitingGames.put(numPlayers, game);
        return game;
    }

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

            // manda sempre lo snapshot corrente — sia che tutti siano riconnessi o no
            return new JoinResult(game.toSnapshot(), allReconnected);
        }
    }

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

    public Game findGame(String gameId){
        Game game = activeGames.get(gameId);
        if(game == null){
            throw new InvalidActionException("Partita non trovata: " + gameId);
        }
        return game;
    }
    @Override
    public GameSnapshot getSnapshotForNickname(String nickname) {
        return getGameByNickname(nickname).toSnapshot();
    }

    @Override
    public boolean isGameFull(String nickname) {
        return getGameByNickname(nickname).isFull();
    }

    public List<String> getPlayersInGame(String nickname){
        Game game = getGameByNickname(nickname);
        return game.getPlayerContext().getPlayers()
                .stream()
                .map(Player::getNickname)
                .toList();
    }

    // metodi per la persistenza del server (SaveGame) e per il DB (createGameResult)
    public void SaveGame(String gameId){ throw new UnsupportedOperationException("TODO - FA persistenza"); }
    public GameResult createGameResult(){ throw new UnsupportedOperationException("TODO - FA leaderboard"); }
}
