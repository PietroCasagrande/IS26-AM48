package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck<T> {
    private final List<T> deck;

    public Deck(List<T> deck) {
        this.deck = List.copyOf(deck);
    }

    // Draws the specified number of cards from the deck
    public List<T> drawCards(int numCards) {
        List<T> drawnCards = new ArrayList<>(this.deck.subList(0, numCards));
        this.deck.subList(0, numCards).clear();
        return drawnCards;
    }

    // getSnapshot
    // public List<T> getSnapshot() {throw new UnsupportedOperationException("TODO");}
}
