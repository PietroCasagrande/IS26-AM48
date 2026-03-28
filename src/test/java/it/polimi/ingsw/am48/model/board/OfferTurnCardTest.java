package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfferTurnCardTest {

    OfferTurnCard offerTurnCard;

    @Test
    void shouldSetupOrderAndTakeTotem() {

        // Food rewards for a 5-player game
        List<Integer> listRewards = List.of(3, 1, 0, 0, -1);
        offerTurnCard = new OfferTurnCard(5, listRewards);

        // Creating list of players
        Player p1 = new Player("alice", Totem.BLACK);
        Player p2 = new Player("bob", Totem.BLUE);
        Player p3 = new Player("charlie", Totem.RED);
        Player p4 = new Player("daniel", Totem.WHITE);
        Player p5 = new Player("eddie", Totem.YELLOW);
        List<Player> players = List.of(p1, p2, p3, p4, p5);

        // Initial random setup
        offerTurnCard.setupOrder(players);

        // Removing all players
        assertNotNull(offerTurnCard.takeTotem(), "First totem should not be null");
        assertNotNull(offerTurnCard.takeTotem(), "Second totem should not be null");
        assertNotNull(offerTurnCard.takeTotem(), "Third totem should not be null");
        assertNotNull(offerTurnCard.takeTotem(), "Fourth totem should not be null");
        assertNotNull(offerTurnCard.takeTotem(), "Fifth totem should not be null");

        // Removing from empty list
        assertThrows(NoSuchElementException.class, () -> {
            offerTurnCard.takeTotem();
        }, "No more totems: should throw NoSuchElementException");
    }

    @Test
    void returnTotem() {
        offerTurnCard = new OfferTurnCard(5, List.of(3, 1, 0, 0, -1));

        // Verifying total calls on Player.addResource method
        Player mockP1 = mock(Player.class);
        offerTurnCard.returnTotem(mockP1);
        verify(mockP1, times(1)).addResource(Resource.FOOD, 3);

        Player mockP2 = mock(Player.class);
        offerTurnCard.returnTotem(mockP2);
        verify(mockP2, times(1)).addResource(Resource.FOOD, 1);

        Player mockP3 = mock(Player.class);
        offerTurnCard.returnTotem(mockP3);
        verify(mockP3, times(1)).addResource(Resource.FOOD, 0);

        Player mockP4 = mock(Player.class);
        offerTurnCard.returnTotem(mockP4);
        verify(mockP4, times(1)).addResource(Resource.FOOD, 0);

        Player mockP5 = mock(Player.class);
        offerTurnCard.returnTotem(mockP5);
        verify(mockP5, times(1)).addResource(Resource.FOOD, -1);
    }
}