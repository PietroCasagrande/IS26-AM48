package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.Optional;

public class OfferCard {
    private final char letterId;
    private final CardStrategy strategy;
    private Optional<Player> totem;

    public OfferCard(char letterId, CardStrategy strategy){
        this.letterId = letterId;
        this.strategy = strategy;
    }

    public Optional<Player> getTotem(){ return totem; }

    // da guardare il tipo di ritorno
    public void placeTotem(Player player){
        throw new UnsupportedOperationException("TODO");
    }

    public Card activateStrategy(Board board, String cardId, Player player, GamePhase phase){
        throw new UnsupportedOperationException("TODO");
    }

    // getSnapshot()

    // removeTotem()
}
