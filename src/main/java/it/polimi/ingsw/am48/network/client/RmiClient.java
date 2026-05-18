package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, VirtualServer {
    /*
    * Il client RMI fa due cose:
    * 1. è un proxy del server -> il client chiama i suoi metodi per mandare comandi
    * 2. riceve il callback -> il server chiama i suoi metodi per mandare aggiornamenti
    */

    private final VirtualServerRmi server;
    private final ClientModel model;

    private final String host;
    private final int port;

    public RmiClient(String host, int port, ClientModel model) throws RemoteException, NotBoundException {
        super();
        this.host = host;
        this.port = port;
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.server = (VirtualServerRmi) registry.lookup("MesosServer");
        this.model = model;
        startHeartbeat();
    }

    // Costruttore package-private per i test di RmiClientTest
    // Testiamo la logica della classe, non la connessione tramite rmi
    // Per farlo ci serve un costruttore semplificato, senza registry e port
    RmiClient(VirtualServerRmi server, ClientModel model) throws RemoteException {
        super();
        this.server = server;
        this.model = model;
        this.host = null;
        this.port = -1;
    }

    // VirtualServer: comandi verso il server dal client
    @Override
    public void joinGame(int numPlayers, String nickname) throws Exception{
        server.connect(nickname, this);
        server.joinGame(numPlayers, nickname);
    }

    @Override
    public void placeTotem(String nickname, char position) throws Exception{
        server.placeTotem(nickname, position);
    }

    @Override
    public void takeCard(String nickname, String cardId) throws Exception{
        server.takeCard(nickname, cardId);
    }


    // VirtualView: callback dal server
    @Override
    public void showGameDelta(GameDelta delta) throws RemoteException{
        model.applyDelta(delta);
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) throws RemoteException{
        model.setInitialState(snapshot);
    }

    @Override
    public void reportError(String errorMessage) throws RemoteException {
        model.notifyError(errorMessage);
    }

    @Override
    public void ping() throws RemoteException {
        // Server pings clients periodically in order to see if they're "still alive".
    }

    private void startHeartbeat(){
        Thread heartbeat = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    server.ping();
                } catch (RemoteException e) {
                    handleServerCrash();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        heartbeat.setDaemon(true);
        heartbeat.start();
    }

    private void handleServerCrash() {
        if(model.isGameEnded()) return;
        // Same message of SocketServerHandler - unified behaviour
        model.notifyError(buildCrashMessage());
        startReconnectWatcher();
    }

    private String buildCrashMessage() {
        String nickname = model.getSessionNickname();
        if(nickname == null) {
            return "\n[!] Lost connection to server. Restart the client once the server comes back online.";
        }
        return String.format("""

            ╔══════════════════════════════════════════════════════╗
            ║  [!] LOST SERVER CONNECTION                          ║
            ║                                                      ║
            ║  Wait for the server to get back online...           ║
            ║  We'll notify you once it'll be available.           ║
            ╚══════════════════════════════════════════════════════╝
            """);
    }

    private void startReconnectWatcher() {
        Thread watcher = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    LocateRegistry.getRegistry(host, port).lookup("MesosServer");
                    // Server came back ON
                    String nickname = model.getSessionNickname();
                    int numPlayers = model.getSessionNumPlayers();
                    model.notifyError(String.format("""
            
                        ╔══════════════════════════════════════════════════════╗
                        ║  [!] SERVER BACK ONLINE                              ║
                        ║                                                      ║
                        ║  Quit the game, restart the client and type:         ║
                        ║       join %s %d                                     ║
                        ║  to get back in the same game.                       ║
                        ║                                                      ║
                        ╚══════════════════════════════════════════════════════╝
                        """, nickname, numPlayers));
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    // server is still down, try again
                }
            }
        });
        watcher.setDaemon(true);
        watcher.start();
    }
}
