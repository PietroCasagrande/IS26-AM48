package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SustenanceStrategyTest {

    private Player p1;
    private Player p2;
    private PlayerContext context;
    private SustenanceStrategy strategy;

    @BeforeEach
    void setUp() {
        p1 = new Player("alice", Totem.BLACK);
        p2 = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(p1);
        context.addPlayer(p1);
        context.addPlayer(p2);

        strategy = new SustenanceStrategy(1, null); // 1 pp lost for each missing food
    }

    private void addCharacters(Player player, int count) {
        for (int i = 0; i < count; i++) {
            player.addToTribe(new CharacterCard("H" + i, Era.FIRST, null, CharacterType.HUNTER, 2));
        }
    }

    @Test
    void shouldPayNoFoodIfNoCharacters() {
        // player with no characters pays no food
        p1.updateFood(3);
        strategy.effect(context);
        assertEquals(3, p1.getFood());
    }

    @Test
    void shouldPayFoodEqualToCharacterCount() {
        // player pays 1 food for each character
        p1.updateFood(5);
        addCharacters(p1, 3);
        strategy.effect(context);
        assertEquals(2, p1.getFood()); // 5 - 3 = 2
    }

    @Test
    void shouldPayExactlyAllFoodIfEqual() {
        // food exactly equal to the number of characters - reaches 0 without losing PP
        p1.updateFood(3);
        addCharacters(p1, 3);
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldLosePPIfFoodInsufficient() {
        // insufficient food - loses ppLostPerChar for each missing food
        p1.updateFood(1);
        addCharacters(p1, 3); // must pay 3, has only 1
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(-2, p1.getPoints()); // missing 2 food × 1 pp
    }

    @Test
    void shouldLoseAllFoodAndAllPPIfNoFood() {
        // player with no food loses pp for each character
        addCharacters(p1, 3);
        strategy.effect(context); // has 0 food, must pay 3
        assertEquals(0, p1.getFood());
        assertEquals(-3, p1.getPoints()); // 3 missing food × 1 pp
    }

    @Test
    void shouldApplyFoodDiscountBeforePaying() {
        // foodDiscount reduces the food to pay
        p1.updateFood(2);
        p1.updateFoodDiscount(2); // discount of 2
        addCharacters(p1, 4); // must pay 4 - 2 = 2
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints()); // enough food after discount
    }

    @Test
    void shouldNotPayIfDiscountCoversAll() {
        // foodDiscount covers all characters, no food paid
        p1.updateFood(3);
        p1.updateFoodDiscount(5);
        addCharacters(p1, 3); // must pay 3 - 5 = negative → 0
        strategy.effect(context);
        assertEquals(3, p1.getFood()); // no food paid
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldAffectAllPlayersIndependently() {
        // each player is evaluated independently
        p1.updateFood(5);
        addCharacters(p1, 2); // p1 pays 2, has 3 left
        p2.updateFood(1);
        addCharacters(p2, 3); // p2 pays 3, has only 1, loses 2 pp
        strategy.effect(context);
        assertEquals(3, p1.getFood());
        assertEquals(0, p1.getPoints());
        assertEquals(0, p2.getFood());
        assertEquals(-2, p2.getPoints());
    }

    @Test
    void shouldLoseMorePPWithHigherPpLostPerChar() {
        SustenanceStrategy heavyStrategy = new SustenanceStrategy(3, null);
        addCharacters(p1, 3);
        heavyStrategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(-9, p1.getPoints());
    }
}