package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a generic deck of cards in the game.
 * <p>
 * This class manages a collection of cards of a specific type, providing operations
 * to draw a defined number of cards from the top of the deck and to track the remaining
 * cards.
 *
 * @param <T> the specific type of {@link Card} contained in this deck
 */
public class Deck<T extends Card> {
    private final List<T> deck;

    /**
     * Creates a new {@code Deck} initialized with the provided list of cards.
     * <p>
     * A defensive copy of the provided list is created to ensure that external
     * modifications do not affect the internal state of the deck.
     *
     * @param deck the initial list of cards to populate the deck
     */
    public Deck(List<T> deck) {
        this.deck = new ArrayList<>(deck);
    }

    /**
     * Draws a specified number of cards from the top of the deck.
     * The drawn cards are subsequently removed from the deck's internal collection.
     *
     * @param numCards the exact number of cards to draw
     * @return a list containing the drawn cards
     * @throws IllegalArgumentException if the requested number is negative or exceeds the total number of currently available cards
     */
    public List<T> drawCards(int numCards) {
        if(numCards < 0 || numCards > deck.size())
            throw new IllegalArgumentException("Cannot draw " + numCards + " cards from the deck: " + this.deck.size() + " cards left");
        List<T> drawnCards = new ArrayList<>(this.deck.subList(0, numCards));
        this.deck.subList(0, numCards).clear();
        return drawnCards;
    }

    /**
     * Retrieves the unique identifiers of all cards currently remaining in the deck.
     *
     * @return a list of strings representing the IDs of the available cards
     */
    public List<String> getRemainingCardIds() {
        return deck.stream()
                .map(Card::getCardId)
                .toList();
    }

}
