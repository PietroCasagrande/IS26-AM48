package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;

import java.util.ArrayList;
import java.util.List;

public class Deck<T extends Card> {
    private final List<T> deck;

    public Deck(List<T> deck) {
        this.deck = new ArrayList<>(deck);
    }

    // Draws the specified number of cards from the deck
    public List<T> drawCards(int numCards) {
        if(numCards < 0 || numCards > deck.size())
            throw new IllegalArgumentException("Cannot draw " + numCards + " cards from the deck: " + this.deck.size() + " cards left");
        List<T> drawnCards = new ArrayList<>(this.deck.subList(0, numCards));
        this.deck.subList(0, numCards).clear();
        return drawnCards;
    }

    // getSnapshot
    // public List<T> getSnapshot() {throw new UnsupportedOperationException("TODO");}
}
