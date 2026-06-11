package it.polimi.ingsw.am48.view.tui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Terminal User Interface (TUI) entry point for Mesos.
 *
 * <p>This class plays two concurrent roles:
 * <ol>
 *   <li><b>Observer role (network thread):</b> implements {@link ModelObserver} and is
 *       registered on {@link ClientModel}. Whenever the server pushes a delta or an initial
 *       snapshot, {@link #onStateUpdated} is invoked on the network thread, which immediately
 *       delegates rendering to {@link CliRenderer}.</li>
 *   <li><b>Input-loop role (main thread):</b> {@link #run()} blocks on stdin waiting for
 *       user commands. Each line is parsed by {@link CliCommandParser} and dispatched to the
 *       appropriate handler, which forwards the action to {@link VirtualServer}.</li>
 * </ol>
 *
 * <p><b>Threading note:</b> the two roles run on different threads but share only
 * {@code localNickname}, which is written once on a successful join and subsequently
 * only read. The field is declared {@code volatile} to guarantee cross-thread visibility
 * without requiring explicit synchronisation.
 *
 * <p>This class does <em>not</em> validate game rules (delegated to the server),
 * format terminal output (delegated to {@link CliRenderer}), or parse raw input strings
 * (delegated to {@link CliCommandParser}).
 */
public class CLIView implements ModelObserver {

    private final VirtualServer server;
    private final ClientModel clientModel;
    private final CliRenderer renderer;
    private final CliCommandParser parser;
    private final BufferedReader reader;

    /** Nickname of the local player. {@code null} until the user successfully sends a join command. */
    private volatile String localNickname;

    /**
     * Constructs the TUI and registers it as an observer of the given client model.
     *
     * @param server the {@link VirtualServer} to which game commands will be sent
     * @param clientModel the local model to observe for state updates
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

    /**
     * Called by {@link ClientModel} whenever a new game state is available, either after
     * a delta or after the initial snapshot has been applied.
     *
     * <p>This method runs on the <em>network thread</em>. It inspects the current phase:
     * if the game has ended it delegates to the game-over renderer; otherwise it delegates
     * to the full-state renderer. The two-step end-game check handles the fact that the
     * {@code EndGameDelta} and the {@code LeaderboardState} may arrive in separate updates.
     *
     * @param state the updated local game state
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

    /**
     * Called by {@link ClientModel} when the server sends an error notification.
     * Delegates display to {@link CliRenderer#renderError}.
     *
     * @param message the human-readable error description received from the server
     */
    @Override
    public void onError(String message) {
        renderer.renderError(message);
    }


    // =========================================================================
    // Input loop — runs on the MAIN thread
    // =========================================================================

    /**
     * Starts the TUI input loop, blocking until the user types {@code quit} or stdin is closed
     * (Ctrl+D on Unix / Ctrl+Z on Windows).
     *
     * <p>While blocked on {@code readLine()}, the network thread can still invoke
     * {@link #onStateUpdated} and print updates to the terminal concurrently.
     * This method must be called from the main thread <em>after</em> the network connection
     * has been established.
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

    // =========================================================================
    // Command dispatching
    // =========================================================================

    /**
     * Parses and dispatches a single input line to the appropriate command handler.
     * After every command (including unrecognised ones), the input prompt is reprinted
     * so the user knows the TUI is ready for the next input.
     *
     * @param line the raw text line read from stdin
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

    /**
     * Handles the {@code join <nickname> <numPlayers>} command.
     *
     * <p>The intended nickname is saved locally <em>before</em> the server call so that
     * the initial snapshot — which may arrive on the network thread almost immediately —
     * can already identify the local player correctly. If the server rejects the join
     * (e.g. duplicate nickname), the error is delivered via {@link #onError} and
     * {@code localNickname} is reset to {@code null} so the user can try again.
     *
     * @param args the command arguments; expects exactly two elements: nickname and numPlayers
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

    /**
     * Handles the {@code place <A-G>} command.
     *
     * <p>Sends a {@code placeTotem} request to the server using the saved
     * {@code localNickname}. The position letter is normalised to uppercase before
     * being forwarded, so both {@code "place a"} and {@code "place A"} are accepted.
     *
     * @param args the command arguments; expects exactly one element: a single letter (A–G)
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

    /**
     * Handles the {@code take <cardId>} command.
     *
     * <p>Sends a {@code takeCard} request to the server for the specified card identifier.
     * Argument validation (e.g. whether the card is actually available) is performed server-side.
     *
     * @param args the command arguments; expects exactly one element: the card identifier
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

    /**
     * Handles the {@code show} command.
     *
     * <p>Retrieves the latest snapshot from {@link ClientModel} and reprints the full
     * game state on demand, without waiting for the next server update. Useful when
     * a server update has scrolled the board off screen.
     */
    private void handleShow() {
        ClientGameState state = clientModel.getState();
        if (state == null) {
            System.out.println("No game state available yet. Wait for the game to start.");
            return;
        }
        renderer.renderState(state, localNickname);
    }

    /**
     * Handles the {@code players} command.
     *
     * <p>Prints a compact summary (nickname, food, prestige points) for all players in the
     * game, without rendering the full board or offer-track details. More readable than
     * {@code show} when only player stats are needed.
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

    /**
     * Handles the {@code info <cardId>} command.
     *
     * <p>Delegates to {@link CliRenderer#renderCardInfo} to display full card details,
     * including the building description when available.
     *
     * @param args the command arguments; expects exactly one element: the card identifier
     */
    private void handleInfo(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: info <cardId>");
            return;
        }
        renderer.renderCardInfo(args[0]);
    }

    /**
     * Handles the {@code leaderboard} command.
     *
     * <p>Prints the historical rankings stored in the server's database.
     * The leaderboard is available only after the game has ended and the server has
     * sent the corresponding update; until then, an informational message is shown.
     */
    private void handleLeaderboard() {
        ClientGameState state = clientModel.getState();
        if(state == null || state.getLeaderboard().isEmpty()) {
            System.out.println("No leaderboard available yet.");
            return;
        }
        renderer.renderLeaderboard(state.getLeaderboard());
    }

    /**
     * Handles the {@code quit} command.
     *
     * <p>Prints a farewell message and terminates the JVM via {@link System#exit}.
     */
    private void handleQuit() {
        System.out.println("Goodbye.");
        System.exit(0);
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Returns {@code true} if the local player has successfully joined a game.
     *
     * <p>If {@code localNickname} is {@code null}, prints an instructional message and
     * returns {@code false}, allowing command handlers to guard themselves with a single
     * {@code if (!isJoined()) return;} line.
     *
     * @return {@code true} if the player has joined; {@code false} otherwise
     */
    private boolean isJoined() {
        if (localNickname == null) {
            System.out.println("You must join a game first. Use: join <nickname> <numPlayers>");
            return false;
        }
        return true;
    }
}