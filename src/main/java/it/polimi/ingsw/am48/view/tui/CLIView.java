package it.polimi.ingsw.am48.view.tui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Terminal User Interface (TUI) for Mesos. The class has two concurrent roles:
 *
 *   1. Observer role (network thread):
 *      Implements ModelObserver and is registered on ClientModel.
 *      When the server sends a delta or snapshot, onStateUpdated() is called
 *      on the network thread. It immediately delegates rendering to CliRenderer.
 *
 *   2. Input loop role (main thread):
 *      The run() method blocks on stdin waiting for user commands.
 *      Each line is parsed by CliCommandParser and dispatched to the
 *      appropriate handler, which calls VirtualServer.
 *
 * Threading note:
 *   The two roles run on different threads but share only 'localNickname', which is written once (on join)
 *   and then only read. No synchronization is needed beyond the volatile declaration.
 *
 * Responsibilities of THIS class:
 *   - Coordinate parser, renderer, and server calls.
 *   - Maintain localNickname after a successful join.
 *   - Keep the input prompt responsive even when updates arrive.
 *
 * What this class does NOT do:
 *   - Validate game rules (delegated to the server).
 *   - Format output (delegated to CliRenderer).
 *   - Parse strings (delegated to CliCommandParser).
 */
public class CLIView implements ModelObserver {

    private final VirtualServer server;
    private final ClientModel clientModel;
    private final CliRenderer renderer;
    private final CliCommandParser parser;
    private final BufferedReader reader;

    // Nickname of the local player. Null until the user successfully sends a join command.
    private volatile String localNickname;

    /*
     * Creates the TUI and registers it as an observer of the client model.
     * Parameters:
     *  - server: the VirtualServer to send commands to
     *  - clientModel: the local model to observe for state updates
     */
    public CLIView(VirtualServer server, ClientModel clientModel) {
        this.server = server;
        this.clientModel = clientModel;
        this.renderer = new CliRenderer();
        this.parser = new CliCommandParser();
        this.reader = new BufferedReader(new InputStreamReader(System.in));

        // Register as observer so onStateUpdated() is called on every server update
        this.clientModel.registerObserver(this);
    }

    // =========================================================================
    // ModelObserver — called by the NETWORK thread
    // =========================================================================

    /*
     * Called by ClientModel whenever a new state is available (after a delta or an initial snapshot has been applied).
     * This method runs on the NETWORK thread, not the main thread. It delegates to the renderer and return.
     */
    @Override
    public void onStateUpdated(ClientGameState state) {
        if ("END_GAME".equals(state.getCurrentPhase())) {
            if(state.getWinnerNickname() != null && state.getLeaderboard().isEmpty()){
                // first update: EndGameDelta is arrived, LeaderboardState not yet
                renderer.renderGameOver(state);
            } else if (!state.getLeaderboard().isEmpty()) {
                System.out.println("  Leaderboard ready. Type 'leaderboard' to view it.");
                System.out.println("\n> ");
            }
            return;
        }
        renderer.renderState(state, localNickname);
    }

    /*
     * Called by ClientModel when the server sends an error notification. Displays the error message and reprints the input prompt.
     */
    @Override
    public void onError(String message) {
        renderer.renderError(message);
    }


    // =========================================================================
    // Input loop — runs on the MAIN thread
    // =========================================================================

    /*
     * Starts the TUI input loop. Doesn't block until the user types "quit" or stdin is closed (Ctrl+D / Ctrl+Z).
     * This method must be called from the main thread AFTER the network connection has been established.
     */
    public void run() {
        renderer.renderHelp();
        System.out.print("> ");

        String line;
        try {
            // readLine() blocks until the user presses Enter.
            // While blocked here, the network thread can still call onStateUpdated() and print updates to the terminal.
            while ((line = reader.readLine()) != null) {
                handleLine(line);
            }
        } catch (IOException e) {
            System.err.println("Input stream error: " + e.getMessage());
        }
    }

    /*
     * Dispatches a single parsed input line to the appropriate handler.
     */
    private void handleLine(String line) {
        var cmd = parser.parse(line);

        switch (cmd.name()) {
            case "join"    -> handleJoin(cmd.args());
            case "place"   -> handlePlace(cmd.args());
            case "take"    -> handleTake(cmd.args());
            case "show"    -> handleShow();
            case "players" -> handlePlayers();
            case "info"    -> handleInfo(cmd.args());
            case "leaderboard" -> handleLeaderboard();
            case "help"    -> renderer.renderHelp();
            case "quit"    -> handleQuit();
            case ""        -> {}  // blank line — just reprint the prompt
            default        -> System.out.println("Unknown command. Type 'help' for the list.");
        }

        // Reprint the prompt after every command so the user knows the TUI is ready
        System.out.print("> ");
    }

    // =========================================================================
    // Command handlers — each called on the MAIN thread
    // =========================================================================

    /*
     * Handles: join <nickname> <numPlayers>
     * Saves the intended nickname locally before sending the command.
     * If the server rejects it (e.g. duplicate nickname), the error arrives via onError() and the user can try again.
     */
    private void handleJoin(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: join <nickname> <numPlayers>");
            return;
        }

        String nickname = args[0];
        int numPlayers;
        try {
            numPlayers = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("numPlayers must be an integer.");
            return;
        }

        // Save nickname now: if the server accepts the join, the initial snapshot
        // will arrive and onStateUpdated() will already have the correct nickname.
        // If the server rejects, the user will call join again with a different name.
        localNickname = nickname;
        // saving the number of Players of the session as well
        clientModel.saveSession(nickname, numPlayers);
        renderer.renderWaiting();

        try {
            server.joinGame(numPlayers, nickname);
        } catch (Exception e) {
            // Join failed at the transport level (not a game-rule error).
            // Game-rule errors (duplicate nickname etc.) come back as ErrorNotification.
            localNickname = null;
            renderer.renderError(e.getMessage());
        }
    }

    /*
     * Handles: place <A-G>
     * Sends a placeTotem command using the saved localNickname.
     */
    private void handlePlace(String[] args) {
        if (!isJoined()) return;

        if (args.length != 1 || args[0].length() != 1) {
            System.out.println("Usage: place <A-G>");
            return;
        }

        char position = Character.toUpperCase(args[0].charAt(0));
        try {
            // localNickname is passed here so the server can identify the player.
            // The user never sees or types the nickname in this command.
            server.placeTotem(localNickname, position);
        } catch (Exception e) {
            renderer.renderError(e.getMessage());
        }
    }

    /*
     * Handles: take <cardId>
     * Sends a takeCard command using the saved localNickname.
     */
    private void handleTake(String[] args) {
        if (!isJoined()) return;

        if (args.length != 1) {
            System.out.println("Usage: take <cardId>");
            return;
        }

        try {
            server.takeCard(localNickname, args[0]);
        } catch (Exception e) {
            renderer.renderError(e.getMessage());
        }
    }

    /*
     * Handles: show
     * Reprints the current game state on demand.
     */
    private void handleShow() {
        ClientGameState state = clientModel.getState();
        if (state == null) {
            System.out.println("No game state available yet. Wait for the game to start.");
            return;
        }
        renderer.renderState(state, localNickname);
    }

    /*
     * Handles: players
     * Prints only the player list (shorter than full "show").
     */
    private void handlePlayers() {
        ClientGameState state = clientModel.getState();
        if (state == null) {
            System.out.println("No game state available yet.");
            return;
        }
        state.getAllPlayers().forEach(p ->
                System.out.println(
                        p.getNickname()
                                + (p.getNickname().equals(localNickname) ? " (YOU)" : "")
                                + "  |  Food: " + p.getFood()
                                + "  |  Points: " + p.getPoints()
                )
        );
    }

    /*
     * Handles: info <cardId>
     * Prints full details of the given card, including building description if available.
     */
    private void handleInfo(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: info <cardId>");
            return;
        }
        renderer.renderCardInfo(args[0]);
    }

    /*
     * Handles: leaderboard
     * Prints the historical table of results stored on server's database
     */
    private void handleLeaderboard() {
        ClientGameState state = clientModel.getState();
        if(state == null || state.getLeaderboard().isEmpty()) {
            System.out.println("No leaderboard available yet.");
            return;
        }
        renderer.renderLeaderboard(state.getLeaderboard());
    }

    /*
     * Handles: quit
     * Exits the application cleanly.
     */
    private void handleQuit() {
        System.out.println("Goodbye.");
        System.exit(0);
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /*
     * Returns true if the local player has joined a game.
     * Prints a message and returns false if not yet joined, so command handlers can guard themselves with a one-liner.
     */
    private boolean isJoined() {
        if (localNickname == null) {
            System.out.println("You must join a game first. Use: join <nickname> <numPlayers>");
            return false;
        }
        return true;
    }
}