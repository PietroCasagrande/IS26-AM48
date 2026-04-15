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

    public ServerMain() {
        this.threadPool = Executors.newCachedThreadPool();
        GameManager gameManager = new GameManager();
        this.mesosServer = new MesosServer(gameManager);
        this.controller = new GameController(gameManager);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione accettata");
                SocketClientHandler handler = new SocketClientHandler(clientSocket, controller, mesosServer);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Errore nel server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
