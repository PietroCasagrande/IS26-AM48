package it.polimi.ingsw.am48;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.RmiClient;
import it.polimi.ingsw.am48.network.client.SocketServerHandler;
import it.polimi.ingsw.am48.view.tui.CLIView;

import java.util.Scanner;

/*
 * Entry point for the Mesos client application.
 *
 * Responsibilities:
 *   - Ask the user to choose network technology (Socket or RMI).
 *   - Ask the user to choose the view type (TUI or GUI).
 *   - Establish the connection to the server.
 *   - Wire together ClientModel, VirtualServer, and the chosen view.
 *
 * This class contains NO game logic. It only assembles the components and hands control to the chosen view.
 */

// To run the client with maven, write
//      mvn exec:java -Dexec.mainClass="it.polimi.ingsw.am48.ClientMain"
// on the terminal, after running ServerMain on another terminal window.

public class ClientMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int    DEFAULT_SOCKET_PORT = 12345;
    private static final int    DEFAULT_RMI_PORT    = 1099;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║         MESOS  CLIENT            ║");
        System.out.println("╚══════════════════════════════════╝");

        // ── 1. Host ───────────────────────────────────────────────────────────
        System.out.print("Server host [" + DEFAULT_HOST + "]: ");
        String host = sc.nextLine().trim();
        if (host.isBlank()) host = DEFAULT_HOST;

        // ── 2. Network technology ─────────────────────────────────────────────
        System.out.println("Network technology:");
        System.out.println("  1) Socket");
        System.out.println("  2) RMI");
        System.out.print("Choice [1]: ");
        String netInput = sc.nextLine().trim();
        int netChoice = netInput.isBlank() ? 1 : Integer.parseInt(netInput);

        // ── 3. View type ──────────────────────────────────────────────────────
        System.out.println("View type:");
        System.out.println("  1) TUI  (terminal)");
        System.out.println("  2) GUI  (JavaFX)  — not yet implemented");
        System.out.print("Choice [1]: ");
        String viewInput = sc.nextLine().trim();
        int viewChoice = viewInput.isBlank() ? 1 : Integer.parseInt(viewInput);

        // ── 4. Build ClientModel ──────────────────────────────────────────────
        // ClientModel is created first because the network handlers need it
        // to deliver incoming snapshots and deltas.
        ClientModel clientModel = new ClientModel();

        // ── 5. Connect to server ──────────────────────────────────────────────
        VirtualServer server;

        if (netChoice == 1) {
            // Socket: SocketServerHandler is both the sender (VirtualServer)
            // and the listener (Runnable). We start the listener thread here.
            System.out.print("Socket port [" + DEFAULT_SOCKET_PORT + "]: ");
            String portInput = sc.nextLine().trim();
            int port = portInput.isBlank() ? DEFAULT_SOCKET_PORT : Integer.parseInt(portInput);

            SocketServerHandler handler = new SocketServerHandler(host, port, clientModel);
            Thread listenerThread = new Thread(handler);
            listenerThread.setDaemon(true); // dies automatically when main exits
            listenerThread.start();
            server = handler;
            System.out.println("Connected via Socket to " + host + ":" + port);

        } else {
            // RMI: RmiClient connects to the registry and returns a VirtualServer stub.
            System.out.print("RMI port [" + DEFAULT_RMI_PORT + "]: ");
            String portInput = sc.nextLine().trim();
            int port = portInput.isBlank() ? DEFAULT_RMI_PORT : Integer.parseInt(portInput);

            server = new RmiClient(host, port, clientModel);
            System.out.println("Connected via RMI to " + host + ":" + port);
        }

        // ── 6. Launch view ────────────────────────────────────────────────────
        if (viewChoice == 1) {
            // TUI: run() blocks on stdin until the user quits.
            new CLIView(server, clientModel).run();

        } else {
            // GUI: placeholder until JavaFX view is implemented.
            System.out.println("GUI not yet implemented. Launching TUI as fallback.");
            new CLIView(server, clientModel).run();
        }
    }
}