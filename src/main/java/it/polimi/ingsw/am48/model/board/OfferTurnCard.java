package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OfferTurnCard {
    private final int numPlayers;
    private final List<Player> positions;
    private final List<Integer> foodRewards;

    public OfferTurnCard(int numPlayers, List<Integer> foodRewards) {
        this.numPlayers = numPlayers;
        this.foodRewards = List.copyOf(foodRewards);
        this.positions = new ArrayList<>();
    }

    // randomizes player order for the very first turn
    public void setupOrder(List<Player> players){
        this.positions.addAll(players);
        Collections.shuffle(this.positions);
    }

    // removes the totem to be placed on the offer track
    public Player takeTotem(){
        return this.positions.removeFirst();
    }

    // replaces the totem at the end of the offer phase
    public void returnTotem(Player player){
        this.positions.add(player);
        player.addResource(Resource.FOOD, this.foodRewards.get(positions.size()-1));
    }

    // getSnapshot
}
