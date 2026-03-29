package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.phase.GamePhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.Optional;

public class OfferCard {
    private final char letterId;                // Letters from A to G, depending on number of players
    private final CardStrategy strategy;
    private Player totem;

    public OfferCard(char letterId, CardStrategy strategy){
        this.letterId = letterId;
        this.strategy = strategy;
        this.totem = null;
    }

    public Optional<Player> getTotem(){ return Optional.of(this.totem); }

    // Place player totem to compute offer phase
    public void placeTotem(Player player){
        if(this.totem != null){ throw new IllegalStateException("Cannot place totem: this position is already occupied"); }
        this.totem = player;
    }

    public Card activateStrategy(Board board, String cardId, Player player, GamePhase phase){
        throw new UnsupportedOperationException("TODO");
    }

    // getSnapshot()

    // removeTotem()
}
