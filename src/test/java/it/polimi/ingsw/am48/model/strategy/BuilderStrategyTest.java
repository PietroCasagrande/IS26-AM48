package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuilderStrategyTest {

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
    void shouldUpdateBuilderPointsCorrectly() {
        BuilderStrategy strategy = new BuilderStrategy(3, 0, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getBuilderPoints());
    }

    @Test
    void shouldUpdateBuildingDiscountCorrectly() {
        BuilderStrategy strategy = new BuilderStrategy(0, 2, null);
        strategy.effect(context);
        assertEquals(2, currPlayer.getBuildingDiscount());
    }

    @Test
    void shouldUpdateBothAttributesCorrectly() {
        BuilderStrategy strategy = new BuilderStrategy(3, 2, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getBuilderPoints());
        assertEquals(2, currPlayer.getBuildingDiscount());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        BuilderStrategy strategy = new BuilderStrategy(3, 2, null);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getBuilderPoints());
        assertEquals(0, otherPlayer.getBuildingDiscount());
    }
}