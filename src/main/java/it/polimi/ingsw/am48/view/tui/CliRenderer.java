package it.polimi.ingsw.am48.view.tui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.view.CardDataRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all terminal output for the TUI.
 *
 * <p>This class is the single point of responsibility for formatting and printing to
 * {@code System.out} and {@code System.err}. It reads data from {@link ClientGameState}
 * and {@link CardDataRegistry} and has no knowledge of the server, the network layer,
 * or the input loop.
 *
 * <p>All {@code render*} methods may be called from any thread (typically the network
 * thread for state updates and the main thread for command feedback), as they perform
 * only sequential writes to the terminal.
 */
public class CliRenderer {

    private final CardDataRegistry cardRegistry;

    /**
     * Constructs a {@code CliRenderer} and initialises the {@link CardDataRegistry}
     * used to resolve human-readable labels and effect descriptions from card identifiers.
     */
    public CliRenderer() {
        this.cardRegistry = new CardDataRegistry();
    }


    // -------------------------------------------------------------------------
    // Full state rendering
    // -------------------------------------------------------------------------

    /**
     * Renders the complete current game state to stdout.
     *
     * <p>Prints a header with the current turn and phase, followed by the board
     * (tribe and building rows), the offer track, the turn order, and the player list.
     * Called every time an update is received from the server.
     *
     * @param state the current local game state
     * @param localNickname the nickname of the local player, used to mark the "(YOU)" label
     */
    public void renderState(ClientGameState state, String localNickname) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║            M E S O S             ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.println("  Turn: " + state.getCurrentTurn()
                + "  |  Phase: " + state.getCurrentPhase());
        System.out.println();

        renderBoard(state);
        renderOfferTrack(state);
        renderTurnOrder(state);
        renderPlayers(state, localNickname);

        System.out.print("\n> ");
    }

    // -------------------------------------------------------------------------
    // Board sections
    // -------------------------------------------------------------------------

    /**
     * Renders the tribe and building showed rows (upper and lower) to stdout.
     *
     * @param state the current local game state
     */
    private void renderBoard(ClientGameState state) {
        System.out.println("── Tribe Showed ─────────────────────");
        System.out.println("  UPPER: " + formatCardList(state.getUpperRowCardIds()));
        System.out.println("  LOWER: " + formatCardList(state.getLowerRowCardIds()));
        System.out.println();
        System.out.println("── Building Showed ──────────────────");
        System.out.println("  UPPER: " + formatCardList(state.getBuildingUpperIds()));
        System.out.println("  LOWER: " + formatCardList(state.getBuildingLowerIds()));
        System.out.println();
    }

    /**
     * Renders the offer track to stdout, showing each position letter (A-G) and the
     * nickname of the player whose totem occupies it, or {@code "-"} if the positon is empty.
     *
     * <p>Only positions present in {@link ClientGameState#getOfferTrackPositions()} are
     * displayed; the server already filters out inactive tiles based on the player count,
     * so no additional filtering is needed here.
     *
     * @param state the current local game state
     */
    private void renderOfferTrack(ClientGameState state) {
        System.out.println("── Offer Track ──────────────────────");
        // map already contains only active positions: no need to filter
        state.getOfferTrackPositions().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String occupant = e.getValue().isEmpty() ? "-" : e.getValue();
                    System.out.println("  [" + e.getKey() + "] " + occupant);
                });
        System.out.println();
    }

    /**
     * Renders the current turn order derived from the OfferTurnCard totem positions.
     *
     * @param state the current local game state
     */
    private void renderTurnOrder(ClientGameState state) {
        System.out.println("── Turn Order ───────────────────────");
        System.out.println("  " + state.getOfferTurnCardOrder());
        System.out.println();
    }

    /**
     * Renders all players with their food, prestige points, totem colour, and acquired
     * card lists. The local player is labelled with "(YOU)". Card lists are omitted when
     * empty to avoid cluttering the output.
     *
     * @param state the current local game state
     * @param localNickname the nickname of the local player
     */
    private void renderPlayers(ClientGameState state, String localNickname) {
        System.out.println("── Players ──────────────────────────");
        for (ClientPlayerState p : state.getAllPlayers()) {
            // Mark the local player so they can immediately see their own status
            String tag = p.getNickname().equals(localNickname) ? " (YOU)" : "";
            System.out.println("  " + p.getNickname() + tag
                    + "  |  Food: " + p.getFood()
                    + "  |  Points: " + p.getPoints()
                    + "  |  Totem: " + p.getTotemColor());

            // Show cards only if the player has some, to avoid cluttering empty lines
            if (!p.getCharacterCardIds().isEmpty()) {
                System.out.println("    Characters: " + p.getCharacterCardIds());
            }
            if (!p.getBuildingCardIds().isEmpty()) {
                System.out.println("    Buildings:  " + p.getBuildingCardIds());
            }
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Messages
    // -------------------------------------------------------------------------

    /**
     * Renders an error message received from the server to {@code System.err},
     * so it stands out visually from normal output. The input prompt is then
     * reprinted so the user can immediately type a corrected command.
     *
     * @param message the human-readable error description
     */
    public void renderError(String message) {
        System.err.println("\n[ERROR] " + message);
        System.out.print("> ");
    }

    /**
     * Renders a waiting message shown in the lobby while not all players have joined yet.
     */
    public void renderWaiting() {
        System.out.println("[INFO] Waiting for other players to join...");
    }

    /**
     * Renders the game-over screen with the winner's name and the final scores
     * sorted in descending order. Also prompts the user to type {@code leaderboard}
     * to view the historical rankings.
     *
     * @param state the final game state, from which the winner and player scores are read
     */
    public void renderGameOver(ClientGameState state) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           GAME  OVER             ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.println("  Winner: " + state.getWinnerNickname());
        System.out.println();
        System.out.println("── Final Scores ────────────────────────");
        state.getAllPlayers().stream()
                .sorted((a,b) -> Integer.compare(b.getPoints(), a.getPoints()))
                .forEach(p -> System.out.println("  " + p.getNickname() + "  -->  " + p.getPoints()));
        System.out.println();
        System.out.println("  Type 'leaderboard' to see the historical rankings.");
        System.out.println("\n> ");
    }

    /**
     * Renders the full details of a single card on demand (invoked by the {@code info} command).
     *
     * <p>Displays the card identifier, its human-readable label from {@link CardDataRegistry},
     * and its effect description when available. If the registry has no entry for the given ID,
     * a default "no additional info" message is shown instead.
     *
     * @param cardId the identifier of the card to display (e.g. {@code "HUN_1"})
     */
    public void renderCardInfo(String cardId){
        String label = cardRegistry.getLabel(cardId);
        String description = cardRegistry.getDescription(cardId);

        System.out.println("\n── Card Info ────────────────────────");
        System.out.println("  " + cardId
                + (label.isEmpty() ? "" : "  [" + label + "]"));

        if (!description.isEmpty()) {
            System.out.println("  Effect: " + description);
        } else {
            System.out.println("  (no additional info available)");
        }
        System.out.println();
        System.out.print("> ");
    }

    /**
     * Renders the historical leaderboard retrieved from the server's database,
     * formatted as a ranked table with columns for ranks, nickname, score and date.
     *
     * @param leaderboard the ordered list of {@link GameResult} entries to display
      */
    public void renderLeaderboard(List<GameResult> leaderboard) {
        System.out.println("\n── Historical Leaderboard ───────────────────────");
        System.out.printf("  %-4s  %-20s  %-8s  %-10s%n", "#", "Nickname", "Score", "Date");
        System.out.println("  ────────────────────────────────────────────────");
        int rank = 1;
        for (GameResult r : leaderboard) {
            System.out.printf("  %-4d  %-20s  %-8d  %-10s%n",
                    rank++,
                    r.getPlayerNickname(),
                    r.getFinalScore(),
                    r.getGameDate().toLocalDate()
            );
        }
        System.out.println();
        System.out.println("  Type 'quit' to exit the server.");
        System.out.println();
        System.out.print("> ");
    }

    /**
     * Renders the help message listing all available TUI commands and their syntax.
     */
    public void renderHelp() {
        System.out.println("""
            Available commands:
              join <nickname> <numPlayers>   — join or create a game
              place <A-G>                    — place your totem on the offer track
              take <cardId>                  — take a card from the showed rows
              show                           — reprint the current game state
              players                        — show only the player list
              info <cardId>                  — show full details of a card
              leaderboard                    — show historical rankings after the game ends
              help                           — show this message
              quit                           — exit the server
            """);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Formats a list of card IDs into a compact inline string, appending the
     * human-readable label from {@link CardDataRegistry} in brackets when available.
     * Returns {@code "(empty)"} if the list is {@code null} or empty.
     *
     * @param ids the list of card identifiers to format
     * @return a single formatted string with entries separated by {@code "  |  "},
     *         or {@code "(empty)"} if the list contains no elements
     */
    private String formatCardList(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "(empty)";

        return ids.stream()
                .map(id -> {
                    String label = cardRegistry.getLabel(id);
                    return label.isEmpty() ? id : id + " [" + label + "]";
                })
                .collect(Collectors.joining("  |  "));
    }
}