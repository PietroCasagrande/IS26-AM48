package it.polimi.ingsw.am48.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) used to deserialize offer turn mechanics from the JSON file.
 * <p>
 * This class holds the configuration for resolving the end of the offer phase,
 * defining the specific food rewards (or costs) and penalties tailored for
 * an exact number of players.
 */
public class OfferTurnCardDTO {

    /** The exact number of players for which this specific turn configuration is valid. */
    public int numPlayers;

    /**
     * A list of values representing the food gained or lost by players.
     * The index in the list corresponds to the player's resolving order.
     */
    public List<Integer> foodRewards;

    /**
     * The prestige points penalty applied (to the last player)
     * if they cannot afford the required food payment.
     */
    public int ppPenalty;
}