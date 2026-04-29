package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

public abstract class Card {
    protected final String cardId;
    protected final Era era;
    protected final CardStrategy strategy;

    protected Card(String cardId, Era era, CardStrategy strategy){
        this.cardId = cardId;
        this.era = era;
        this.strategy = strategy;
    }

    public String getCardId() { return cardId; }
    public Era getEra() { return era; }
    public CardStrategy getStrategy() { return strategy; }

    public abstract void acquire(PlayerContext playerContext);
}
