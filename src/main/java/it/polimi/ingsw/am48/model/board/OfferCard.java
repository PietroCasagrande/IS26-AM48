package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;

import java.util.Optional;

public class OfferCard {
    private final char letterId;                // Letters from A to G, depending on number of players
    private final int numUp;        // quante carte da fila superiore
    private final int numDown;      // quante carte da fila inferiore
    private final int foodBonus;    // cibo dato (3 per tessera A, 0 per le altre)
    private final int numPlayers;
    private Player totem;

    public OfferCard(char letterId, int numUp, int numDown, int foodBonus, int numPlayers) {
        this.letterId = letterId;
        this.numUp = numUp;
        this.numDown = numDown;
        this.foodBonus = foodBonus;
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

    public int getTotalPicks(){
        return this.numUp + this.numDown;
    }

    public Optional<Player> getTotem(){
        return Optional.ofNullable(this.totem);
    }

    // Getters
    public char getLetterId() {return this.letterId;}
    public int getNumUp(){return this.numUp;}
    public int getNumDown(){return this.numDown;}
    public int getNumPlayers(){return this.numPlayers;}
    public int getFoodBonus(){return this.foodBonus;}


    /*
    // To implement: waiting for PickCardStrategy definition
    public Card activateStrategy(Board board, String cardId, Player player, GamePhase phase){
        throw new UnsupportedOperationException("TODO");
    } */

    // getSnapshot()
}
