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

    // getSnapshot
}
