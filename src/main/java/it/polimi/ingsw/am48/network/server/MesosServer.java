package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MesosServer {
    // Map thread-safe per gestire le connessioni concorrenti
    private final Map<String, VirtualView> connectedPlayers = new ConcurrentHashMap<>();
    // MesosServer non deve conoscere il gameManager del model

    public void registerClient(String nickname, VirtualView view) {
        connectedPlayers.put(nickname, view);
    }

    public void broadcastToGame(List<String> recipients, GameDelta delta) {
        for (String nickname : recipients) {
            VirtualView view = connectedPlayers.get(nickname);
            if (view != null) {
                try { view.showGameDelta(delta); }
                catch (Exception e) {
                    /* handle disconnect */
                    // unregisterClient(nickname)
                }
            }
        }
    }

    // overload per GameSnapshot (Scenario 1B, joingame e set up partita, snapshot completo)
    public void broadcastSnapshotToGame(List<String> recipients, GameSnapshot snapshot) {
        for (String nickname : recipients) {
            VirtualView view = connectedPlayers.get(nickname);
            if (view != null) {
                try { view.showInitialSnapshot(snapshot); }
                catch (Exception e) {
                    /* handle disconnect */
                    // unregisterClient(nickname)
                }
            }
        }
    }
}
