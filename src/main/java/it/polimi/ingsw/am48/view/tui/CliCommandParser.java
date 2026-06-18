package it.polimi.ingsw.am48.view.tui;

import java.util.Arrays;

/**
 * Parses raw text lines typed by the user into structured {@link ParsedCommand} objects.
 *
 * <p>This class is responsible exclusively for <em>syntactic</em> parsing: it splits an
 * input line into a command name and its arguments, normalising the command name to lowercase.
 * It performs no game-rule validation and no argument-semantic checks (e.g. whether a given
 * card ID exists in the current game state); all such validation is delegated to the server.
 */
public class CliCommandParser {

    /**
     * Immutable result of parsing a single input line.
     *
     *  @param name the command keyword, always in lowercase (e.g. {@code "place"}, {@code "take"})
     *  @param args the tokens that follow the command name; may be empty but is never {@code null}
     */
    public record ParsedCommand(String name, String[] args) {}

    /**
     * Parses a single line of user input into a {@link ParsedCommand}.
     *
     * <p>Null or blank lines are treated as empty commands (name {@code ""}, empty args array)
     * so the caller can safely ignore them without additional null-cheks. Extra whitespace
     * between tokens is collapsed automatically.
     *
     * <p>Examples:
     * <pre>
     *   "place A"           -> name="place",  args=["A"]
     *   "take HUN_1"        -> name="take",   args=["HUN_1"]
     *   "join Pietro 2"     -> name="join",   args=["Pietro", "2"]
     *   ""  or "   "        -> name="",       args=[]
     * </pre>
     *
     * @param line the raw text line read from stdin; may be {@code null} or blank
     * @return a {@link ParsedCommand} with the normalised command name and its arguments
     */
    public ParsedCommand parse(String line) {
        // Treat null or blank lines as empty commands so the caller can ignore them safely
        if (line == null || line.isBlank()) {
            return new ParsedCommand("", new String[0]);
        }

        // Split on any whitespace sequence (handles extra spaces between tokens)
        String[] tokens = line.trim().split("\\s+");

        String name = tokens[0].toLowerCase();
        // Everything after the first token is an argument
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        return new ParsedCommand(name, args);
    }
}