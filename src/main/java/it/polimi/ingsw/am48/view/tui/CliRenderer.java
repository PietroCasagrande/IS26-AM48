package it.polimi.ingsw.am48.view.tui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.view.CardDataRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Handles all terminal output for the TUI. Responsibilities:
 *   - Render the full game state (board, players, offer track, etc.).
 *   - Render error messages received from the server.
 *   - Render informational messages (waiting, help, etc.).
 *
 * This class has NO knowledge of the server, the network, or the input loop.
 * It only reads ClientGameState and writes to System.out / System.err.
 */
public class CliRenderer {

    private final CardDataRegistry cardRegistry;

    public CliRenderer() {
        this.cardRegistry = new CardDataRegistry();
    }

    /*
     * Full state rendering:
     *  - Renders the complete current game state to stdout.
     *  - Called every time an update is received from the server.
     * Parameters:
     *  - state: the current local game state
     *  - localNickname: the nickname of the local player (used to mark "YOU")
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

    /*
     * Renders the tribe and building showed rows.
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

    /*
     * Renders the offer track (letters A-G and which player's totem is there).
     * Empty positions are shown as "-".
     * Uses getActivePositions util method to print only the "active" tiles in relation to the number of players.
     */
    private void renderOfferTrack(ClientGameState state) {
        System.out.println("── Offer Track ──────────────────────");
        // La mappa contiene già solo le posizioni attive — nessun filtro necessario
        state.getOfferTrackPositions().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String occupant = e.getValue().isEmpty() ? "-" : e.getValue();
                    System.out.println("  [" + e.getKey() + "] " + occupant);
                });
        System.out.println();
    }

    /*
     * Renders the current turn order (the OfferTurnCard totem positions).
     */
    private void renderTurnOrder(ClientGameState state) {
        System.out.println("── Turn Order ───────────────────────");
        System.out.println("  " + state.getOfferTurnCardOrder());
        System.out.println();
    }

    /*
     * Renders all players with their food, prestige points, and cards.
     * The local player is marked with "(YOU)".
     * The parameter localNickname it's used to highlight the local player
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

    /*
     * Renders an error message received from the server.
     * Uses System.err so it stands out visually from normal output.
     */
    public void renderError(String message) {
        System.err.println("\n[ERROR] " + message);
        System.out.print("> ");
    }

    /*
     * Renders a waiting message shown while the lobby is filling up.
     */
    public void renderWaiting() {
        System.out.println("[INFO] Waiting for other players to join...");
    }

    /*
     * Renders a game-over screen with the final scores.
     * The only parameter is state, from which we'll get all the attributes needed.
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

    // Renders the full details of a card on demand (used by the "info" command)
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

    // Renders the historical table of results for the games with the specified num of players
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

    /*
     * Renders the help message listing all available commands.
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

    /*
     * Formats a list of card IDs into a compact inline string.
     * Appends the registry label in brackets when available.
     */
    private String formatCardList(java.util.List<String> ids) {
        if (ids == null || ids.isEmpty()) return "(empty)";

        return ids.stream()
                .map(id -> {
                    String label = cardRegistry.getLabel(id);
                    return label.isEmpty() ? id : id + " [" + label + "]";
                })
                .collect(Collectors.joining("  |  "));
    }

    /*
    * Tells renderOfferTrack which tiles are active during the game.
    * It does a switch case over the number of players of the game.
    * */
//    private Set<Character> getActivePositions(int numPlayers) {
//        return switch (numPlayers) {
//            case 2 -> Set.of('B', 'C', 'E', 'F');
//            case 3 -> Set.of('B', 'C', 'D', 'E', 'F');
//            case 4 -> Set.of('B', 'C', 'D', 'E', 'F', 'G');
//            default -> Set.of('A', 'B', 'C', 'D', 'E', 'F', 'G');
//        };
//    }
}