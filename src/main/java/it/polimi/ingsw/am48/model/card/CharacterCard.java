package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;

public class CharacterCard extends Card {
    private final CharacterType type;
    private final int minPlayers;

    public CharacterCard(String cardId, Era era, CardStrategy strategy,
                         CharacterType type, int minPlayers) {
        super(cardId, era, strategy);
        this.type = type;
        this.minPlayers = minPlayers;
    }

    // capiamo se servono sti getter, non credo
    // public CharacterType getType() { return type; }
    // public int getMinPlayers() { return minPlayers; }

    @Override
    public void onPlay(Player player, List<Player> allPlayers) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void acquire(Player player, Showed<Card> showedList) {
        throw new UnsupportedOperationException("TODO");
    }

}
