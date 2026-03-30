package it.polimi.ingsw.am48.model.game;

import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.phase.WaitingForPlayersPhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {
    private final String gameId;
    private final int numPlayers;
    private PlayerContext playerContext;
    private Board board;
    private NotificatorCenter notificatorCenter;
    private GamePhase currentPhase;
    private int currentTurn;

    public Game(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.currentTurn = 0;
        this.currentPhase = new WaitingForPlayersPhase(numPlayers);
    }

    public void addPlayer(String nickname) { currentPhase.addPlayer(this, nickname); }
    public GameDelta placeTotem(Player player, char position) { return currentPhase.placeTotem(this, player, position); }
    public GameDelta takeCard(Player player, String cardId) { return currentPhase.takeCard(this, player, cardId); }
    public GameDelta endTurn() { return currentPhase.endTurn(this); }
    // void setupGame() ??? che dobbiamo fare


    public Board getBoard() { return board; }
    /*
    Tutti gli altri getter, togliamo se non servono (manca sempre lo snapshot col metodo toSnapshot())

    public String getGameId() { return gameId; }
    public int getNumPlayers() { return numPlayers; }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public NotificatorCenter getNotificatorCenter() { return notificatorCenter; }
    public GamePhase getCurrentPhase() { return currentPhase; }
    public int getCurrentTurn() { return currentTurn; }
    public void incrementTurn() { currentTurn++; }
    public boolean isFull() { return players.size() == numPlayers; }
     */


    public boolean checkEnd(){ throw new UnsupportedOperationException("TODO"); }
    public GameDelta computeEndGameScore(List<Player> players){ throw new UnsupportedOperationException("TODO"); }
    public Player findWinner(){ throw new UnsupportedOperationException("TODO"); }


    // Package-private: solo le Phase cambiano fase
    void setPhase(GamePhase phase) { this.currentPhase = phase; }
    // void setBoard(Board board) { this.board = board; }
    // void setNotificatorCenter(NotificatorCenter nc) { this.notificatorCenter = nc; }
    // List<Player> getMutablePlayers() { return players; }
}
