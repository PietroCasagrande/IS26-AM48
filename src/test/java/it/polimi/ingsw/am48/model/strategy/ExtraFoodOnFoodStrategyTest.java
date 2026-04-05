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
        // player con diritto all'extra food riceve 1 cibo
        currPlayer.setExtraFoodRight(true);
        strategy.effect(context);
        assertEquals(1, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveExtraFoodIfNotDeserves() {
        // player senza diritto all'extra food non riceve nulla
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldStackWithExistingFood() {
        // il cibo extra si somma a quello già posseduto
        currPlayer.updateFood(3);
        currPlayer.setExtraFoodRight(true);
        strategy.effect(context);
        assertEquals(4, currPlayer.getFood()); // 3 + 1 extra
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        currPlayer.setExtraFoodRight(true);
        otherPlayer.updateFood(2);
        strategy.effect(context);
        assertEquals(2, otherPlayer.getFood()); // invariato
    }

    @Test
    void shouldNotGiveExtraFoodIfRightIsFalseByDefault() {
        // il diritto è false di default, nessun cibo extra
        assertFalse(currPlayer.deservesExtraFood());
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }
}