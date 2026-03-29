package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OfferCardTest {

    CardStrategy mockCardStrategy = mock(CardStrategy.class);
    OfferCard offerCard = new OfferCard('A', mockCardStrategy, 5);
    Player mockP1 = mock(Player.class);

    @Test
    void shouldPlaceTotemAndThrowExceptionIfOccupied() {

        // Creating second player
        Player mockP2 = mock(Player.class);

        // First player places their totem
        offerCard.placeTotem(mockP1);

        // Second player tries to place their totem in the same place
        assertThrows(IllegalStateException.class, () -> offerCard.placeTotem(mockP2),
                "Place already occupied: should throw IllegalStateException");
    }

    @Test
    void shouldReturnNullIfTotemNotPresent() {
        // Returning totem from an empty offer card
        assertEquals(Optional.empty(), offerCard.returnTotem());
    }

    @Test
    void shouldReturnTotemIfPresent() {
        // Returning player from an occupied offer card
        offerCard.placeTotem(mockP1);
        assertEquals(Optional.of(mockP1), offerCard.returnTotem());

        // Verifying offer card emptiness
        assertEquals(Optional.empty(), offerCard.returnTotem());
    }

    @Test
    void shouldNotGetTotemIfNotPresent() {
        // Getting totem from an empty offer card
        assertEquals(Optional.empty(), offerCard.getTotem());
    }

    @Test
    void shouldGetTotemIfPresent() {
        // Getting totem from an occupied offer card
        offerCard.placeTotem(mockP1);
        assertEquals(Optional.of(mockP1), offerCard.getTotem());
    }
    /*
    @Test
    void activateStrategy() {
    } */
}