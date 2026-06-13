package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.JsonGameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;
import it.polimi.ingsw.am48.repository.MySqlLeaderboardRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point and lifecycle manager for the Mesos server.
 *
 * <p>On construction, wires together the full server-side stack: a {@link JsonGameRepository}
 * for crash-recovery persistence, a {@link MySqlLeaderboardRepository} for the historical
 * leaderboard, a {@link GameManager} backed by both repositories, a {@link GameController}
 * on top of it, and a transport-agnostic {@link MesosServer} hub. Any games persisted from a
 * previous run are reloaded immediately via {@link GameManager#loadCrashedGames()}, so
 * reconnecting players can resume.
 *
 * <p>{@link #start()} brings up both network transports:
 * <ul>
 *   <li><b>RMI</b> is started first because binding a registry is non-blocking: a
 *       {@link RmiServer} is exported and bound under the name {@code "MesosServer"}.</li>
 *   <li><b>Socket</b> is started second and blocks the calling thread in an
 *       {@code accept()} loop; each accepted connection is handed to a new
 *       {@link SocketClientHandler} and executed on a cached thread pool.</li>
 * </ul>
 *
 * <p>To run the server with Maven:
 * <pre>{@code mvn exec:java -Dexec.mainClass="it.polimi.ingsw.am48.network.server.ServerMain"}</pre>
 * Start this before launching any client, and recompile after any change.
 */
public class ServerMain {
    private static final int PORT = 12345; // server's port
    private static final int RMI_PORT = 1099; // server's rmi port
    private final ExecutorService threadPool;
    private final MesosServer mesosServer;
    private final GameController controller;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final JsonGameRepository gameRepository;
    private final MySqlLeaderboardRepository leaderboardRepository;
    private static final String SAVES_DIR = "saves/";

    /**
     * Assembles the server-side stack and reloads any games persisted from a previous run.
     *
     * <p>Constructs, in order: a cached thread pool for Socket client handlers, the
     * {@link JsonGameRepository} (reading/writing snapshots under {@code saves/}), the
     * {@link MySqlLeaderboardRepository} (opening a HikariCP pool to MySQL), the
     * {@link GameManager} wired to both repositories, and the {@link GameController}.
     * Finally calls {@link GameManager#loadCrashedGames()} so that games interrupted by a
     * previous crash are registered and ready for player reconnection.
     */
    public ServerMain() {
        this.threadPool = Executors.newCachedThreadPool();
        this.gameRepository = new JsonGameRepository(SAVES_DIR);
        this.leaderboardRepository = new MySqlLeaderboardRepository();
        GameManager gameManager = new GameManager(gameRepository, leaderboardRepository);
        gameManager.loadCrashedGames();
        this.mesosServer = new MesosServer();
        this.controller = new GameController(gameManager);
    }

    /**
     * Starts both network transports and blocks until the server is stopped.
     *
     * <p>First, an {@link RmiServer} is created and bound in the RMI registry on
     * {@link #RMI_PORT} under the name {@code "MesosServer"}; RMI failures are logged but
     * do not prevent the Socket transport from starting. Then a {@link ServerSocket} is
     * opened on {@link #PORT} and the calling thread enters an {@code accept()} loop: each
     * accepted connection is wrapped in a {@link SocketClientHandler} and submitted to the
     * thread pool, running until {@link #stop()} closes the server socket.
     */
    public void start() {
        running = true;

        // RMI is started first because it doesn't block anything, unlike Socket that remains in accept()'s loop
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

    /**
     * Stops the server: closes the leaderboard's database connection pool and closes the
     * Socket server socket, which unblocks {@link #start()}'s {@code accept()} loop by
     * raising a {@link java.net.SocketException} on the next iteration.
     *
     * <p>Does not unbind the RMI registry, as the JVM is expected to terminate shortly after.
     */
    public void stop() {
        running = false;
        leaderboardRepository.close();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Sblocca la accept() forzando una SocketException
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Application entry point: constructs a {@code ServerMain} and starts it,
     * blocking the main thread in the Socket {@code accept()} loop.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        new ServerMain().start();
    }
}
