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

        when(cardA.getCardId()).thenReturn("A");
        when(cardB.getCardId()).thenReturn("B");
        when(cardC.getCardId()).thenReturn("C");
        when(cardD.getCardId()).thenReturn("D");
        when(cardE.getCardId()).thenReturn("E");

        deck = new Deck<>(List.of(cardA, cardB, cardC, cardD, cardE));
    }

    // ==================== constructor ====================

    @Test
    @DisplayName("constructor: should initialize deck with the correct elements")
    void shouldInitializeDeckWithCorrectElements() {
        List<Card> drawn = deck.drawCards(5);
        assertEquals(List.of(cardA, cardB, cardC, cardD, cardE), drawn);
    }

    @Test
    @DisplayName("constructor: should defensively copy the input list")
    void shouldDefensivelyCopyInputList() {
        List<Card> source = new ArrayList<>(List.of(cardA, cardB, cardC));
        Deck<Card> deckFromMutableList = new Deck<>(source);
        source.add(mock(Card.class));

        List<Card> drawn = deckFromMutableList.drawCards(3);
        assertEquals(List.of(cardA, cardB, cardC), drawn);
    }

    // ==================== drawCards ====================

    @Test
    @DisplayName("drawCards: should return the correct cards from the top of the deck")
    void shouldDrawCorrectCardsFromTop() {
        List<Card> drawn = deck.drawCards(2);
        assertEquals(List.of(cardA, cardB), drawn);
    }

    @Test
    @DisplayName("drawCards: should remove drawn cards from the deck")
    void shouldRemoveDrawnCardsFromDeck() {
        deck.drawCards(2);
        List<Card> remaining = deck.drawCards(3);
        assertEquals(List.of(cardC, cardD, cardE), remaining);
    }

    @Test
    @DisplayName("drawCards: should throw IllegalArgumentException when drawing from empty deck")
    void shouldThrowWhenDrawingFromEmptyDeck() {
        deck.drawCards(5);
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(1));
    }

    @Test
    @DisplayName("drawCards: should display correct message when drawing from empty deck")
    void shouldDisplayCorrectExceptionMessageWhenDeckEmpty() {
        deck.drawCards(5);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> deck.drawCards(1));
        assertEquals("Cannot draw 1 cards from the deck: 0 cards left", ex.getMessage());
    }

    @Test
    @DisplayName("drawCards: should return empty list and leave deck untouched when drawing zero cards")
    void shouldReturnEmptyListWhenDrawingZeroCards() {
        List<Card> drawn = deck.drawCards(0);
        assertTrue(drawn.isEmpty());

        List<Card> remaining = deck.drawCards(5);
        assertEquals(List.of(cardA, cardB, cardC, cardD, cardE), remaining);
    }

    @Test
    @DisplayName("drawCards: should throw IllegalArgumentException when drawing more cards than available")
    void shouldThrowWhenDrawingMoreCardsThanAvailable() {
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(10));
    }

    @Test
    @DisplayName("drawCards: should throw IllegalArgumentException when drawing a negative number of cards")
    void shouldThrowWhenDrawingNegativeNumberOfCards() {
        assertThrows(IllegalArgumentException.class, () -> deck.drawCards(-1));
    }

    @Test
    @DisplayName("drawCards: exception message should contain requested and available count")
    void shouldIncludeRequestedAndAvailableCountInExceptionMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> deck.drawCards(10));
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("5"));
    }

    // ==================== getRemainingCardIds ====================

    @Test
    @DisplayName("getRemainingCardIds: should return all card ids when no cards have been drawn")
    void shouldReturnAllCardIdsWhenNoneDrawn() {
        List<String> ids = deck.getRemainingCardIds();
        assertEquals(List.of("A", "B", "C", "D", "E"), ids);
    }

    @Test
    @DisplayName("getRemainingCardIds: should return remaining card ids after some cards are drawn")
    void shouldReturnRemainingCardIdsAfterDraw() {
        deck.drawCards(2);
        List<String> ids = deck.getRemainingCardIds();
        assertEquals(List.of("C", "D", "E"), ids);
    }

    @Test
    @DisplayName("getRemainingCardIds: should return empty list when all cards are drawn")
    void shouldReturnEmptyListWhenAllCardsDrawn() {
        deck.drawCards(5);
        assertTrue(deck.getRemainingCardIds().isEmpty());
    }
}