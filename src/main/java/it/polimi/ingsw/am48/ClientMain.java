package it.polimi.ingsw.am48;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.RmiClient;
import it.polimi.ingsw.am48.network.client.SocketServerHandler;
import it.polimi.ingsw.am48.view.gui.GUIViewFxml;
import it.polimi.ingsw.am48.view.tui.CLIView;
import javafx.application.Application;

import java.util.Scanner;

/**
 * Entry point for the Mesos client application.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Ask the user to choose the network technology (Socket or RMI).</li>
 *   <li>Ask the user to choose the view type (TUI or GUI).</li>
 *   <li>Establish the connection to the server.</li>
 *   <li>Wire together {@link ClientModel}, {@link VirtualServer}, and the chosen view.</li>
 * </ul>
 *
 * <p>This class contains <b>no game logic</b>: it only assembles the components and hands
 * control to the chosen view.
 *
 * <p>To run the client with Maven, after starting {@code ServerMain} on another terminal:
 * <pre>{@code mvn exec:java -Dexec.mainClass="it.polimi.ingsw.am48.ClientMain"}</pre>
 *
 * @see it.polimi.ingsw.am48.network.server.ServerMain
 * @see LauncherClient
 */
public class ClientMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int    DEFAULT_SOCKET_PORT = 12345;
    private static final int    DEFAULT_RMI_PORT    = 1099;

    /**
     * Interactively prompts the user for connection and view settings, then assembles and
     * launches the client.
     *
     * <p>The setup proceeds in six steps:
     * <ol>
     *   <li><b>Host:</b> read the server hostname, defaulting to {@value #DEFAULT_HOST}.</li>
     *   <li><b>Network technology:</b> Socket or RMI, defaulting to Socket.</li>
     *   <li><b>View type:</b> TUI or GUI, defaulting to TUI.</li>
     *   <li><b>{@link ClientModel}:</b> created first, since the network handlers need it
     *       to deliver incoming snapshots and deltas as soon as the connection is open.</li>
     *   <li><b>Connection:</b> for Socket, a {@link SocketServerHandler} is created and its
     *       listener loop started on a daemon thread; for RMI, a {@link RmiClient} is
     *       constructed, which performs the registry lookup itself. Either way, the result
     *       is exposed uniformly as a {@link VirtualServer}.</li>
     *   <li><b>View:</b> for TUI, {@link CLIView#run()} is called directly, blocking the
     *       main thread on stdin; for GUI, the model and server are passed statically to
     *       {@link GUIViewFxml} before launching the JavaFX {@link Application}.</li>
     * </ol>
     *
     * @param args command-line arguments, forwarded unchanged to {@link Application#launch}
     *             when the GUI view is selected
     * @throws Exception if the connection to the server cannot be established
     *                    (e.g. socket connection refused, or RMI registry/lookup failure)
     */
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
        System.out.println("  2) GUI  (JavaFX)");
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
        }
        else if(viewChoice == 2) {
            GUIViewFxml.setModel(clientModel);
            GUIViewFxml.setServer(server);
            Application.launch(GUIViewFxml.class, args);
        }
    }
}