package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// To run the server with maven, write
//      mvn exec:java -Dexec.mainClass="it.polimi.ingsw.am48.network.server.ServerMain"
// on the terminal, before starting ClientMain on another terminal window.
// Remember to compile after any change before starting the server.

public class ServerMain {
    private static final int PORT = 12345; // Porta del server
    private static final int RMI_PORT = 1099; // Porta del server tramite RMI
    private final ExecutorService threadPool;
    private final MesosServer mesosServer;
    private final GameController controller;
    private ServerSocket serverSocket;
    private volatile boolean running;
    // GameRepository gameRepository = new JsonGameRepository(...);
    // LeaderboardRepository leaderboardRepository = new MySqlLeaderboardRepository(...);

    public ServerMain() {
        this.threadPool = Executors.newCachedThreadPool();
        // parametri da sostituire
        GameManager gameManager = new GameManager(null, null);
        // GameManager gameManager = new GameManager(JsonGameRepository, MySqlLeaderboardRepository);
        this.mesosServer = new MesosServer();
        this.controller = new GameController(gameManager);
    }

    public void start() {
        running = true;

        // Avvio RMI prima perché NON è bloccante (a differenza di socket che permane nel loop accept())
        try {
            RmiServer rmiServer = new RmiServer(controller, mesosServer);
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind("MesosServer", rmiServer);
            System.out.println("RMI Server avviato sulla porta: " + RMI_PORT);
        } catch (RemoteException e) {
            System.err.println("Errore avvio RMI: " + e.getMessage());
        }

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server avviato sulla porta " + PORT);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione accettata");
                SocketClientHandler handler = new SocketClientHandler(clientSocket, controller, mesosServer);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            if(!serverSocket.isClosed())
                System.err.println("Errore nel server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Sblocca la accept() forzando una SocketException
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
