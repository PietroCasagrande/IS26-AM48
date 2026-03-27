package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;

public abstract class Card {
    private final String cardId;
    private final Era era;
    private final CardStrategy strategy;

    protected Card(String cardId, Era era, CardStrategy strategy){
        this.cardId = cardId;
        this.era = era;
        this.strategy = strategy;
    }

    public String getCardId() { return cardId; }
    public Era getEra() { return era; }
    public CardStrategy getStrategy() { return strategy; }

    public abstract void onPlay(Player player, List<Player> allPlayers);
    public abstract void acquire(Player player, Showed<Card> showedList);
}
