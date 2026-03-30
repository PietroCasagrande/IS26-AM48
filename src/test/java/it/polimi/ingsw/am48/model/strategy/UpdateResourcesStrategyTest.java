package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateResourcesStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);
    }

    @Test
    void shouldUpdateFoodCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD, 3, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldUpdateShamanStarsCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.STAR, 2, null);
        strategy.effect(context);
        assertEquals(2, currPlayer.getShamanStars());
    }

    @Test
    void shouldUpdateFoodDiscountCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD_DISCOUNT, 3, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFoodDiscount());
    }

    @Test
    void shouldThrowOnInvalidResource() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.PRESTIGE_POINT, 1, null);
        assertThrows(IllegalArgumentException.class, () -> strategy.effect(context));
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD, 3, null);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}
