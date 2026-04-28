package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

public class CharacterCard extends Card {
    private final CharacterType type;
    private final int minPlayers;

    public CharacterCard(String cardId, Era era, CardStrategy strategy,
                         CharacterType type, int minPlayers) {
        super(cardId, era, strategy);
        this.type = type;
        this.minPlayers = minPlayers;
    }

    // getter() per il tipo di personaggio e il numero minimo di giocatori
    public CharacterType getType() { return type; }
    public int getMinPlayers() { return minPlayers; }

    @Override
    public void acquire(Player player) {
        player.addToTribe(this);
    }

}
