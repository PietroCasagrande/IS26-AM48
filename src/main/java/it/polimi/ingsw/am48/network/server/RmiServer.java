package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {
    private final GameController controller;
    private final MesosServer mesosServer;
    private final Map<String, VirtualViewRmi> rmiCallbacks = new ConcurrentHashMap<>();

    public RmiServer(GameController controller, MesosServer mesosServer) throws RemoteException {
        super();
        this.controller = controller;
        this.mesosServer = mesosServer;
    }

    @Override
    public void connect(String nickname, VirtualViewRmi client) throws RemoteException{
        rmiCallbacks.put(nickname, client);

        // this thread is executed each time RmiViewAdapter detects a disconnection
        Runnable onDisconnect = () -> {
            rmiCallbacks.remove(nickname);
            List<String> companions = controller.handleClientDisconnect(nickname);
            mesosServer.broadcastErrorToGame(companions,
                    "Player '" + nickname + "' disconnected. The game has been terminated.");
            mesosServer.unregisterClient(nickname);
        };

        VirtualView viewAdapter = new RmiViewAdapter(client, onDisconnect);
        mesosServer.registerClient(nickname, viewAdapter);
    }

    @Override
    public void joinGame(int numPlayers, String nickname) throws RemoteException {
        try {
            JoinResult result = controller.handleJoinGame(numPlayers, nickname);
            if (result.gameStarted()) {
                List<String> recipients = controller.getPlayersInGame(nickname);
                mesosServer.broadcastSnapshotToGame(recipients, result.snapshot());
            } else {
                rmiCallbacks.get(nickname).showInitialSnapshot(result.snapshot());
            }
        } catch (Exception e) {
            try{rmiCallbacks.get(nickname).reportError(e.getMessage()); }
            catch (RemoteException ex) {ex.printStackTrace();}
        }
    }

    @Override
    public void placeTotem(String nickname, char position) throws RemoteException {
        try {
            List<GameDelta> deltas = controller.handlePlaceTotem(nickname, position);
            List<String> recipients = controller.getPlayersInGame(nickname);
            for(GameDelta d : deltas){
                mesosServer.broadcastToGame(recipients, d);
            }
        } catch (Exception e) {
            try { rmiCallbacks.get(nickname).reportError(e.getMessage());}
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @Override
    public void takeCard(String nickname, String cardId) throws RemoteException {
        try {
            List<GameDelta> deltas = controller.handleTakeCard(nickname, cardId);
            List<String> recipients = controller.getPlayersInGame(nickname);
            for (GameDelta delta : deltas) {
                mesosServer.broadcastToGame(recipients, delta);
            }
        } catch (Exception e) {
            try { rmiCallbacks.get(nickname).reportError(e.getMessage());}
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @Override
    public void ping() throws RemoteException {
        // empty method: if the server's alive, it answers. Otherwise, RemoteException.
    }
}
