package it.polimi.ingsw.am48.dto;

/**
 * Data Transfer Object (DTO) used to deserialize card blueprints from the JSON configuration file.
 * <p>
 * This class acts as a generic, "fat" data container representing any type of card
 * in the game (Character, Event, or Building). Depending on the specific type of card
 * being parsed, some fields may remain unpopulated (null or zero). It is the responsibility
 * of the specific factories to extract and validate the required fields.
 */
public class CardDTO {

    /** The unique string identifier of the card. */
    public String id;

    /** The era to which this card belongs (e.g., "FIRST", "SECOND"). */
    public String era;

    /** The minimum number of players required in the game for this card to be included in the deck. */
    public int minPlayers;

    /** The specific type of the character (applicable only to Character cards). */
    public String character;

    /** The specific category of the event (applicable only to Event cards). */
    public String eventType;

    /** The amount of food required to acquire the card (used for Building cards). */
    public int foodCost;

    /** The amount of prestige points immediately granted or inherently worth by the card. */
    public int prestigePoints;

    /**
     * The blueprint defining the card's active effect.
     * If the card has no special effect, this field may be null.
     */
    public StrategyDTO strategy;
}
