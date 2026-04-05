package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class OfferTurnCard {
    private final int numPlayers;
    private final List<Player> order;
    private final List<Integer> foodRewards;            // must be 3 positive numbers
    private final int ppPenalty;                        // positive number for penalty
    private final List<Integer> initialFoodRewards;     // initial food

    public OfferTurnCard(int numPlayers, List<Integer> foodRewards, int  ppPenalty) {
        this.numPlayers = numPlayers;
        this.foodRewards = List.copyOf(foodRewards);
        this.ppPenalty = ppPenalty;
        this.order = new ArrayList<>();
        this.initialFoodRewards = new ArrayList<>(List.of(2, 3, 3, 4, 4));
    }

    // Randomizes player order for the very first turn
    public void setupOrder(List<Player> players){
        this.order.addAll(players);
        Collections.shuffle(this.order);
        for(int i = 0; i < this.order.size(); i++){
            this.order.get(i).updateFood(this.initialFoodRewards.get(i));
        }
    }

    // Removes the totem to be placed on the offer track
    public Player takeTotem(){
        return this.order.removeFirst();
    }

    // Replaces the totem at the end of the offer phase:
    // the first player or the first two players get food (or 0), depending on the number of players
    // the last player loses food and gets penalty if they haven't enough food
    public void returnTotem(Player player){
        this.order.add(player);
        int position = this.order.size();
        player.setExtraFoodRight(false);    // set the extra food right to default value to avoid errors from the previous turn

        if(position <= 2) {
            player.updateFood(this.foodRewards.get(position-1));
            if(foodRewards.get(position-1) != 0) player.setExtraFoodRight(true);    // player deserves extra food only if he gets food from the offer turn card (not when he gets 0)
        }

        if(position == this.numPlayers) {
            player.payFood(this.foodRewards.getLast(), this.ppPenalty);
        }
    }

    public OfferTurnCardSnapshot toSnapshot() {
        List<String> totemOrder = order.stream()
                .map(p -> p.getTotem().name())
                .toList();

        return new OfferTurnCardSnapshot(totemOrder);
    }

    // Getters for testing
    public int getNumPlayers(){return  this.numPlayers;}
    public List<Integer> getFoodRewards(){return  this.foodRewards;}
    public int getPpPenalty(){return  this.ppPenalty;}
}
