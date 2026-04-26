package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.phase.WaitingForPlayersPhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.List;

public class Game {
    private final String gameId;
    private final int numPlayers;
    private final PlayerContext playerContext;
    private Board board;
    private final NotificatorCenter notificatorCenter;
    private GamePhase currentPhase;
    private int currentTurn;

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


    // Package-private: only Phases set phases
    public void setPhase(GamePhase phase) { this.currentPhase = phase; }
    void setBoard(Board board) { this.board = board; }
    public void incrementTurn() { this.currentTurn++; }

    public GameSnapshot toSnapshot(){
        List<PlayerSnapshot> playerSnapshots = playerContext.getPlayers().stream()
                .map(Player::toSnapshot)
                .toList();

        return new GameSnapshot(
                gameId,
                numPlayers,
                currentTurn,
                playerSnapshots,
                // board!=null is necessary because during WaitingForPlayersPhase the board has not been created yet
                board != null ? board.toSnapshot() : null,
                currentPhase.toSnapshot()
        );
    }
}
