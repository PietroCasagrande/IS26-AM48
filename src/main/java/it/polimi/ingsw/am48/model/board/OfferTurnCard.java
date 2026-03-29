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
    private final List<Integer> foodRewards;

    public OfferTurnCard(int numPlayers, List<Integer> foodRewards) {
        this.numPlayers = numPlayers;
        this.foodRewards = List.copyOf(foodRewards);
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

    // replaces the totem at the end of the offer phase
    public void returnTotem(Player player){
        this.order.add(player);
        player.addResource(Resource.FOOD, this.foodRewards.get(order.size()-1));
    }

    // getSnapshot
}
