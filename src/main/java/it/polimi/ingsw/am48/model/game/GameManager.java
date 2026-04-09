package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameManager implements ModelInterface{
    private final Map<String, Game> activeGames;
    private final Map<Integer, Game> waitingGames;
    private final Map<String, Game> playerToGame;
    private int nextGameId;

    // repository per le funzionalità avanzate
    private GameRepository gameRepository;
    private LeaderboardRepository leaderboardRepository;

    public GameManager(){
        this.activeGames = new HashMap<>();
        this.waitingGames = new HashMap<>();
        this.playerToGame = new HashMap<>();
        this.nextGameId = 1;
    }

    // ModelInterface implementation: joinGame, placeTotem and takeCard methods
    @Override
    public void joinGame(int numPlayers, String nickname){
        if(playerToGame.containsKey(nickname)){
            throw new InvalidActionException("Nickname già in uso: " + nickname);
        }

        Game game = findAvailableGame(numPlayers).orElseGet(() -> createGame(numPlayers));

        game.addPlayer(nickname);
        playerToGame.put(nickname, game);

        if(game.isFull()){
            waitingGames.remove(numPlayers);
            activeGames.put(game.getGameId(), game);
        }
    }

    @Override
    public GameDelta placeTotem(String nickname, char position){
        Game game = getGameByNickname(nickname);
        Player player = game.getPlayerByNickname(nickname);
        return game.placeTotem(player, position);
    }

    @Override
    public List<GameDelta> takeCard(String nickname, String cardId){
        Game game = getGameByNickname(nickname);
        Player player = game.getPlayerByNickname(nickname);
        return game.takeCard(player, cardId);
    }

    // Game lookup:
    // metodo getGameByNickname ritorna il game in cui sta giocando il player con nickname passato x parametro
    private Game getGameByNickname(String nickname){
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

    public Game findGame(String gameId){
        Game game = activeGames.get(gameId);
        if(game == null){
            throw new InvalidActionException("Partita non trovata: " + gameId);
        }
        return game;
    }

    // metodi per la persistenza del server (SaveGame) e per il DB (createGameResult)
    public void SaveGame(String gameId){ throw new UnsupportedOperationException("TODO - FA persistenza"); }
    public GameResult createGameResult(){ throw new UnsupportedOperationException("TODO - FA leaderboard"); }
}
