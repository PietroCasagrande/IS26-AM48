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

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Game {
    private final String gameId;
    private final int numPlayers;
    private PlayerContext playerContext;
    private Board board;
    private final NotificatorCenter notificatorCenter;
    private GamePhase currentPhase;
    private int currentTurn;
    private Set<String> reconnectedPlayers;

    public Game(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.playerContext = new PlayerContext();
        this.notificatorCenter = new NotificatorCenter();
        this.currentTurn = 1;
        this.currentPhase = new WaitingForPlayersPhase(numPlayers);
    }

    // Methods exposed to the controller
    public void addPlayer(String nickname) {
        currentPhase.addPlayer( playerContext,this, nickname);
    }

    public List<GameDelta> placeTotem(Player player, char position) {
        return currentPhase.placeTotem(this, player, position);
    }
    public List<GameDelta> takeCard(Player player, String cardId) {
        return currentPhase.takeCard(this, player, cardId);
    }

    // Player lookup, needed by GameManager's methods
    public Player getPlayerByNickname(String nickname){
        return playerContext.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new InvalidActionException("Giocatore non trovato: " + nickname));
    }

    // getters
    public Board getBoard() { return board; }
    public String getGameId() { return gameId; }
    public int getNumPlayers() { return numPlayers; }
    public PlayerContext getPlayerContext() { return playerContext; }
    public NotificatorCenter getNotificatorCenter() { return notificatorCenter; }
    public GamePhase getCurrentPhase() { return currentPhase; }
    public int getCurrentTurn() { return currentTurn; }
    public boolean isFull() { return playerContext.getPlayers().size() == numPlayers; }

    // End game logic: checkEnd() and computeEndGameScore() should already be managed by GamePhase
    // public boolean checkEnd(){ throw new UnsupportedOperationException("TODO"); }
    // public GameDelta computeEndGameScore(List<Player> players){ throw new UnsupportedOperationException("TODO"); }
    public Player findWinner(){
        return playerContext.getPlayers().stream()
                .max((p1,p2) -> Integer.compare(p1.getPoints(), p2.getPoints()))
                .orElseThrow(() -> new InvalidActionException("Nessun giocatore in partita."));
    }

    // segna il giocatore come riconnesso
    public void markReconnected(String nickname) {
        reconnectedPlayers.add(nickname);
    }

    // true se tutti i player della partita si sono riconnessi
    public boolean allPlayersReconnected() {
        return playerContext.getPlayers().stream()
                .map(Player::getNickname)
                .allMatch(reconnectedPlayers::contains);
    }

    // Package-private: only Phases set phases
    public void setPhase(GamePhase phase) { this.currentPhase = phase; }
    public void setBoard(Board board) { this.board = board; }
    public void incrementTurn() { this.currentTurn++; }

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

    public static Game fromSnapshot(GameSnapshot snapshot){
        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(snapshot.getNumPlayers());
        Game game = new Game(snapshot.getGameId(), snapshot.getNumPlayers());

        game.playerContext = PlayerContext.fromSnapshot(snapshot.getPlayerContext(), cardMap);
        List<Player> players = game.playerContext.getPlayers();

        game.board = Board.fromSnapshot(snapshot.getBoard(), cardMap,  players);

        game.currentPhase = GamePhase.fromSnapshot(snapshot.getPhase(), players, game.numPlayers);

        game.currentTurn = snapshot.getCurrentTurn();

        // registriamo nuovamente le strategy dei buildings di tutti i player, altrimenti non verranno mai notificate (characters non si registrano)
        game.playerContext.getPlayers().forEach(p -> {
            p.getTribe().getBuildings().forEach(b -> b.getStrategy().registerTo(game.notificatorCenter, game.playerContext));
        });

        return game;
    }
}
