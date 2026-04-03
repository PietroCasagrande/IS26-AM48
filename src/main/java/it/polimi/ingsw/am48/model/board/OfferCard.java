package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.Optional;

public class OfferCard {
    private final char letterId;                // Letters from A to G, depending on number of players
    private final CardStrategy strategy;
    private final int numPlayers;
    private Player totem;

    public OfferCard(char letterId, CardStrategy strategy, int numPlayers) {
        this.letterId = letterId;
        this.strategy = strategy;
        this.numPlayers = numPlayers;
        this.totem = null;
    }

    // Places player totem to compute offer phase
    public void placeTotem(Player player){
        if(this.totem != null){ throw new IllegalStateException("Cannot place totem: this position is already occupied"); }
        this.totem = player;
    }

    // Returns totem after offer phase
    public Optional<Player> returnTotem(){
        Optional<Player> removedTotem = Optional.ofNullable(this.totem);
        this.totem = null;
        return removedTotem;
    }

    public Optional<Player> getTotem(){
        return Optional.ofNullable(this.totem);
    }

    // Getters for testing
    public char getLetterId() {return this.letterId;}
    public int getNumPlayers(){return this.numPlayers;}
    public CardStrategy getStrategy(){return this.strategy;}

    /*
    // To implement: waiting for PickCardStrategy definition
    public Card activateStrategy(Board board, String cardId, Player player, GamePhase phase){
        throw new UnsupportedOperationException("TODO");
    } */

    // getSnapshot()
}
