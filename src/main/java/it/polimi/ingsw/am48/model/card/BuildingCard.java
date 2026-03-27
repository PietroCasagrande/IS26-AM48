package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
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

    public int getFoodCost() { return foodCost; }
    public int getPrestigePoints() { return prestigePoints; }

    @Override
    public void onPlay(Player player, List<Player> allPlayers) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void acquire(Player player, Showed<Card> showedList) {
        throw new UnsupportedOperationException("TODO");
    }
}
