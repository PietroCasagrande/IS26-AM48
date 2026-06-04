package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;

import java.util.Optional;

/**
 * Represents a single Offer Card on the game board.
 * <p>
 * Each card defines the rules for a specific slot during the offer phase,
 * including the number of cards a player can draw from the upper and lower rows,
 * and any immediate food bonuses. It also tracks which player's totem is currently
 * placed on it.
 */
public class OfferCard {
    private final char letterId;
    private final int numUp;
    private final int numDown;
    private final int foodBonus;
    private final int numPlayers;
    private Player totem;

    /**
     * Creates a new {@code OfferCard} with the specified attributes.
     *
     * @param letterId   the identifier of the card (A to G, depending on the number of players)
     * @param numUp      the number of cards the player can pick from the upper row
     * @param numDown    the number of cards the player can pick from the lower row
     * @param foodBonus  the amount of food awarded immediately upon choosing this card (only for A)
     * @param numPlayers the number of players in the current game
     */
    public OfferCard(char letterId, int numUp, int numDown, int foodBonus, int numPlayers) {
        this.letterId = letterId;
        this.numUp = numUp;
        this.numDown = numDown;
        this.foodBonus = foodBonus;
        this.numPlayers = numPlayers;
        this.totem = null;
    }

    /**
     * Places a player's totem on this offer card to reserve it for the offer phase.
     *
     * @param player the player placing the totem
     * @throws IllegalStateException if this card is already occupied by a totem
     */
    public void placeTotem(Player player){
        if(this.totem != null){ throw new IllegalStateException("Cannot place totem: this position is already occupied"); }
        this.totem = player;
    }

    /**
     * Removes the totem from this card after the offer phase is completed.
     *
     * @return an {@link Optional} containing the removed {@link Player}, or an empty {@code Optional} if no totem was present
     */
    public Optional<Player> returnTotem(){
        Optional<Player> removedTotem = Optional.ofNullable(this.totem);
        this.totem = null;
        return removedTotem;
    }

    /**
     * Calculates the total number of cards the player is allowed to pick by summing
     * the upper and lower row allowances.
     *
     * @return the total number of picks available for this card
     */
    public int getTotalPicks(){
        return this.numUp + this.numDown;
    }

    /**
     * @return an {@link Optional} containing the player occupying this card, or an empty {@code Optional} if unoccupied
     */
    public Optional<Player> getTotem(){
        return Optional.ofNullable(this.totem);
    }

    public char getLetterId() {
        return this.letterId;
    }

    public int getNumUp(){
        return this.numUp;
    }

    public int getNumDown(){
        return this.numDown;
    }

    public int getNumPlayers(){
        return this.numPlayers;
    }

    public int getFoodBonus(){
        return this.foodBonus;
    }
}
