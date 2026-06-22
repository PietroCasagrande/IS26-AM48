package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExtraFoodOnFoodStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private ExtraFoodOnFoodStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new ExtraFoodOnFoodStrategy(null);
    }

    @Test
    void shouldGiveExtraFoodIfDeserves() {
        // player entitled to extra food receives 1 food
        currPlayer.setExtraFoodRight(true);
        strategy.effect(context);
        assertEquals(1, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveExtraFoodIfNotDeserves() {
        // player not entitled to extra food receives nothing
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldStackWithExistingFood() {
        // the extra food adds to the food already held
        currPlayer.updateFood(3);
        currPlayer.setExtraFoodRight(true);
        strategy.effect(context);
        assertEquals(4, currPlayer.getFood()); // 3 + 1 extra
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        currPlayer.setExtraFoodRight(true);
        otherPlayer.updateFood(2);
        strategy.effect(context);
        assertEquals(2, otherPlayer.getFood()); // unchanged
    }

    @Test
    void shouldNotGiveExtraFoodIfRightIsFalseByDefault() {
        // the right is false by default, no extra food
        assertFalse(currPlayer.deservesExtraFood());
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }
}