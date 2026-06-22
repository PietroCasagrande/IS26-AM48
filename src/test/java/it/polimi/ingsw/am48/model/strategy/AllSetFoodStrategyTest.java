package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AllSetFoodStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private AllSetFoodStrategy strategy;
    private NotificatorCenter mockNotificatorCenter; // mock declaration

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        mockNotificatorCenter = mock(NotificatorCenter.class);
        strategy = new AllSetFoodStrategy(null);
    }

    private void addOneOfEachType() {
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("Bu1", Era.FIRST, null, CharacterType.BUILDER, 2));
        currPlayer.addToTribe(new CharacterCard("I1", Era.FIRST, null, CharacterType.INVENTOR, 2));
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("P1", Era.FIRST, null, CharacterType.PICKER, 2));
        currPlayer.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
    }

    @Test
    void shouldGiveNoFoodIfSetNotCompleted() {
        // incomplete tribe - some types are missing
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFiveFoodWhenFirstSetCompleted() {
        // a complete set of 6 different types grants 5 food
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(5, currPlayer.getFood());
    }

    @Test
    void shouldGiveTenFoodWhenTwoSetsCompleted() {
        // two sets completed at different times grant 10 food in total
        addOneOfEachType();
        strategy.effect(context); // first set
        addOneOfEachType();
        strategy.effect(context); // second set
        assertEquals(10, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodIfNoNewSetCompleted() {
        // adding a character of a type already present does not complete a new set
        addOneOfEachType();
        strategy.effect(context); // first set completed, 5 food
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        strategy.effect(context); // no new set
        assertEquals(5, currPlayer.getFood()); // stays 5
    }

    @Test
    void shouldNotGiveFoodForSetsAlreadyPresentAtAcquisition() {
        // sets already present before acquisition must not count
        addOneOfEachType();
        strategy.registerTo(mockNotificatorCenter, context); // simulates acquisition
        strategy.effect(context); // no new set since acquisition
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFoodOnlyForNewSetsAfterAcquisition() {
        // pre-existing sets do not count, only new ones after acquisition
        addOneOfEachType();
        strategy.registerTo(mockNotificatorCenter, context); // acquisition with 1 set already present
        addOneOfEachType();
        strategy.effect(context); // new set completed after acquisition
        assertEquals(5, currPlayer.getFood()); // only 1 new set × 5 food
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}
