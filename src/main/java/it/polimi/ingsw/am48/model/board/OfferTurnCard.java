package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.ArrayList;
import java.util.List;

public class OfferTurnCard {
    private final int numPlayers;
    private final List<Player> positions;
    private final CardStrategy effect;

    public OfferTurnCard(int numPlayers, CardStrategy effect){
        this.numPlayers = numPlayers;
        this.positions = new ArrayList<>();
        this.effect = effect;
    }

    // metodi della OfferTurnCard ???

    // getSnapshot
}
