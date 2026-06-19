package it.polimi.ingsw.am48.model.enums;

/**
 * Totem colours assigned to players at the start of the game.
 *
 * <p>Each player owns exactly one totem of a distinct colour, used to identify
 * their marker on the offer track and on the turn-order card throughout the game.
 * The number of colours in use depends on the number of players; unused colours
 * are never assigned.
 */
public enum Totem {
    BLUE,
    RED,
    BLACK,
    WHITE,
    YELLOW
}
