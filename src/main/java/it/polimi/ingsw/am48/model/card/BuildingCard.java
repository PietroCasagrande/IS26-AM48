package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;

public class BuildingCard extends Card{
    private final int foodCost;
    private final int prestigePoints;

    public BuildingCard(String cardId, Era era, CardStrategy strategy,
                        int foodCost, int prestigePoints) {
        super(cardId, era, strategy);
        this.foodCost = foodCost;
        this.prestigePoints = prestigePoints;
    }

    // getter per il costo dell'edificio (costo in cibo) e i punti che assegna
    public int getFoodCost() { return foodCost; }
    public int getPrestigePoints() { return prestigePoints; }

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
}
