package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckTest {

    private Card cardA;
    private Card cardB;
    private Card cardC;
    private Card cardD;
    private Card cardE;
    private Deck<Card> deck;

    @BeforeEach
    void setUp() {
        cardA = mock(Card.class);
        cardB = mock(Card.class);
        cardC = mock(Card.class);
        cardD = mock(Card.class);
        cardE = mock(Card.class);

        deck = new Deck<>(List.of(cardA, cardB, cardC, cardD, cardE));
    }

    // ==================== Constructor ====================

    @Test
    @DisplayName("constructor: deck is initialized with the correct elements")
    void shouldInitializeDeckWithCorrectElements() {
        List<Card> drawn = deck.drawCards(5);
        assertEquals(List.of(cardA, cardB, cardC, cardD, cardE), drawn);
    }

    @Test
    @DisplayName("constructor: modifying the original list does not affect the deck")
    void shouldDefensivelyCopyInputList() {
        List<Card> source = new ArrayList<>(List.of(cardA, cardB, cardC));
        Deck<Card> deckFromMutableList = new Deck<>(source);
        source.add(mock(Card.class));

        List<Card> drawn = deckFromMutableList.drawCards(3);
        assertEquals(List.of(cardA, cardB, cardC), drawn);
    }

    // ==================== drawCards ====================

    @Test
    @DisplayName("drawCards: returns the correct cards from the top of the deck")
    void shouldDrawCorrectCards() {
        List<Card> drawn = deck.drawCards(2);
        assertEquals(List.of(cardA, cardB), drawn);
    }

    @Test
    @DisplayName("drawCards: removes drawn cards from the deck")
    void shouldRemoveCardsFromDeck() {
        deck.drawCards(2);
        List<Card> remaining = deck.drawCards(3);
        assertEquals(List.of(cardC, cardD, cardE), remaining);
    }

    @Test
    @DisplayName("drawCards: drawing all cards empties the deck")
    void shouldThrowIllegalArgumentExceptionWhenDrawingAllCards() {
        deck.drawCards(5);
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(1));
    }

    @Test
    @DisplayName("drawCards: display the correct message when drawing from empty deck")
    void shouldDisplayCorrectExceptionMessage() {
        deck.drawCards(5);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> deck.drawCards(1));
        assertEquals("Cannot draw 1 cards from the deck: 0 cards left", exception.getMessage());
    }

    @Test
    @DisplayName("drawCards: drawing zero cards returns an empty list without modifying the deck")
    void shouldReturnEmptyListWhenDrawingZeroCards() {
        List<Card> drawn = deck.drawCards(0);
        assertTrue(drawn.isEmpty());

        List<Card> remaining = deck.drawCards(5);
        assertEquals(List.of(cardA, cardB, cardC, cardD, cardE), remaining);
    }

    @Test
    @DisplayName("drawCards: drawing more cards than available throws IllegalArgumentException")
    void shouldThrowIllegalArgumentExceptionWhenDrawingMoreCards() {
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(10));
    }

    @Test
    @DisplayName("drawCards: drawing a negative number of cards throws IllegalArgumentException")
    void shouldThrowIllegalArgumentExceptionWhenDrawingNegativeCards() {
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(-1));
    }

    @Test
    @DisplayName("drawCards: exception message contains the number of cards requested and available")
    void shouldDisplayCorrectNumberOfCardsWhenThrowingException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> deck.drawCards(10));
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("5"));
    }
}