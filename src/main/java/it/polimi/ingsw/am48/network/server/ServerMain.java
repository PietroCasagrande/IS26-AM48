package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.GameManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int PORT = 12345; // Porta del server
    private final ExecutorService threadPool;
    private final MesosServer mesosServer;
    private final GameController controller;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public ServerMain() {
        this.threadPool = Executors.newCachedThreadPool();
        GameManager gameManager = new GameManager();
        this.mesosServer = new MesosServer();
        this.controller = new GameController(gameManager);
    }

    public void start() {
        running = true;
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
