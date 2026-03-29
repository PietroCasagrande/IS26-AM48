package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class OfferTurnCard {
    private final int numPlayers;
    private final List<Player> order;
    private final List<Integer> foodRewards;        // must be 3 numbers
    private final int ppPenalty;                    // negative number for penalty

    public OfferTurnCard(int numPlayers, List<Integer> foodRewards, int  ppPenalty) {
        this.numPlayers = numPlayers;
        this.foodRewards = List.copyOf(foodRewards);
        this.ppPenalty = ppPenalty;
        this.order = new ArrayList<>();
    }

    // randomizes player order for the very first turn
    public void setupOrder(List<Player> players){
        this.order.addAll(players);
        Collections.shuffle(this.order);
    }

    // removes the totem to be placed on the offer track
    public Player takeTotem(){
        return this.order.removeFirst();
    }

    // replaces the totem at the end of the offer phase:
    // the first player or the first two players get food, depending on the number of players
    // the last player loses food and gets penalty if they haven't enough food
    public void returnTotem(Player player){
        this.order.add(player);
        if(this.order.size() <= 2) player.updateFood(this.foodRewards.get(order.size()-1));
        else if (this.order.size() == this.numPlayers) player.payFood(this.foodRewards.get(order.size()-1), this.ppPenalty);
    }

    // getSnapshot
}
