package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

/**
 * Represents a Building card within the game.
 * <p>
 * Building cards provide prestige points and usually possess delayed or conditional
 * strategic effects. Unlike Character cards, acquiring a Building requires paying a
 * base cost in food resources, which can be dynamically discounted based on the
 * builders in player's tribe.
 */
public class BuildingCard extends Card{
    private final int foodCost;
    private final int prestigePoints;

    /**
     * Constructs a new {@code BuildingCard} with its specific attributes.
     *
     * @param cardId         the unique string identifier for this card
     * @param era            the chronological era to which this card belongs
     * @param strategy       the strategy logic associated with this building
     * @param foodCost       the base amount of food required to acquire this building
     * @param prestigePoints the amount of prestige points granted by this building
     */
    public BuildingCard(String cardId, Era era, CardStrategy strategy,
                        int foodCost, int prestigePoints) {
        super(cardId, era, strategy);
        this.foodCost = foodCost;
        this.prestigePoints = prestigePoints;
    }

    /**
     * Executes the acquisition transaction for the building card.
     * <p>
     * This method dynamically calculates the actual cost of the building by applying
     * any available building discounts currently granted by the player's tribe.
     * It strictly enforces that the final cost cannot be negative. If the player possesses
     * sufficient food, the resources are deducted, and the building is added to the tribe.
     *
     * @param playerContext the context and state of the player attempting to buy the building
     * @throws InvalidActionException if the player does not have enough food to cover the actual cost
     */
    @Override
    public void acquire(PlayerContext playerContext) {

        Player player = playerContext.getCurrPlayer();
        int actualCost = Math.max(0, this.foodCost - player.getTribe().getBuildingDiscount());
        if(player.getFood() < actualCost){
            throw new InvalidActionException(
                    "Non hai il cibo sufficiente: richiesto " + actualCost + ", disponibile " + player.getFood());
        }
        player.updateFood(-actualCost);
        player.addToTribe(this);
    }

    public int getFoodCost() { return foodCost; }
    public int getPrestigePoints() { return prestigePoints; }
}
