package it.polimi.ingsw.am48.dto;

/**
 * Data Transfer Object (DTO) used to deserialize offer card configurations from the JSON file.
 * <p>
 * This class holds the blueprint for a single slot on the offer track, defining its
 * identifier, the amount of cards a player can draw, its bonuses, and the number of players required.
 */
public class OfferCardDTO {

    /** The unique character identifier representing the position on the offer track (e.g., 'A', 'B'). */
    public char id;

    /** The number of cards a player is allowed to pick from the upper row. */
    public int numUp;

    /** The number of cards a player is allowed to pick from the lower row. */
    public int numDown;

    /** The amount of immediate food resources granted when placing a totem on this card. */
    public int foodBonus;

    /** The minimum number of players required in the game for this offer slot to be available. */
    public int minPlayers;
}