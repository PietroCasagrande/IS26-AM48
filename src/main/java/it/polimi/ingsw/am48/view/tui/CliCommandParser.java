package it.polimi.ingsw.am48.view.tui;

import java.util.Arrays;

/*
 * Parses raw text lines typed by the user into structured commands.
 *
 * Responsibilities:
 *   - Split the input line into command name and arguments.
 *   - Normalize the command name to lowercase.
 *   - Perform ONLY syntactic parsing: does NOT validate game rules,
 *     does NOT check argument semantics (e.g. whether a card ID exists).
 *      --> All game-rule validation is delegated to the server.
 */
public class CliCommandParser {

    /*
     * Represents the result of a parsed input line:
     *  - name: the command name, always lowercase (e.g. "place", "take")
     *  - args: the arguments following the command name (might be empty)
     */
    public record ParsedCommand(String name, String[] args) {}

    /*
     * Parses a single line of user input. Examples:
     *   "place A"           -> name="place",  args=["A"]
     *   "take HUN_1"        -> name="take",   args=["HUN_1"]
     *   "join Pietro 2"     -> name="join",   args=["Pietro", "2"]
     *   ""  or "   "        -> name="",       args=[]   (empty line, ignored by CLIView)
     * Parameter line: the raw input line read from stdin
     * Returns a ParsedCommand with name and args extracted
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