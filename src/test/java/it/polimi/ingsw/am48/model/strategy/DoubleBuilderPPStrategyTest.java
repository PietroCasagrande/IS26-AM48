package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleBuilderPPStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private DoubleBuilderPPStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new DoubleBuilderPPStrategy(null);
    }

    @Test
    void shouldDoubleBuilderPoints() {
        // builderPoints doubles after the effect
        currPlayer.updateBuilderPoints(4);
        strategy.effect(context);
        assertEquals(8, currPlayer.getBuilderPoints());
    }

    @Test
    void shouldGiveZeroIfNoBuilderPoints() {
        // doubling zero stays zero
        strategy.effect(context);
        assertEquals(0, currPlayer.getBuilderPoints());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        currPlayer.updateBuilderPoints(4);
        otherPlayer.updateBuilderPoints(4);
        strategy.effect(context);
        assertEquals(4, otherPlayer.getBuilderPoints()); // unchanged
    }
}