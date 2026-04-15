package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MesosServer {
    // Map thread-safe per gestire le connessioni concorrenti
    private Map<String, VirtualView> connectedPlayers = new ConcurrentHashMap<>();
    private GameManager gameManager; // Il root del modello lato server

    public MesosServer(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void registerClient(String nickname, VirtualView view) {
        connectedPlayers.put(nickname, view);
    }

    public void broadcastToGame(String nickname, GameDelta delta) {
        Game game = gameManager.getGameByNickname(nickname);
        for (Player p : game.getPlayerContext().getPlayers()) {
            VirtualView view = connectedPlayers.get(p.getNickname());
            if (view != null) {
                try { view.showGameDelta(delta); } catch (Exception e) {

                    /* handle disconnect */ }
            }
        }
    }

    // overload per GameSnapshot (Scenario 1B, joingame e set up partita, snapshot completo)
    public void broadcastSnapshotToGame(String nickname, GameSnapshot snapshot) {
        Game game = gameManager.getGameByNickname(nickname);
        for (Player p : game.getPlayerContext().getPlayers()) {
            VirtualView view = connectedPlayers.get(p.getNickname());
            if (view != null) {
                try { view.showInitialSnapshot(snapshot); } catch (Exception e) {

                    /* handle disconnect */ }
            }
        }
    }

    public void broadcastToAll(String message) {
        for (VirtualView view : connectedPlayers.values()) {
            try {
                view.reportError(message); // O un metodo specifico per broadcast globale
            } catch (Exception e) {

                // handle

            }
        }
    }
}
