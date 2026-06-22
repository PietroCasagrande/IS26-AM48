package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraPickStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private ExtraPickStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new ExtraPickStrategy(null);
    }

    @Test
    void shouldBeFalseBeforeEffect() {
        // the right to an extra pick is false by default
        assertFalse(currPlayer.deservesExtraPick());
    }

    @Test
    void shouldSetExtraPickRightToTrue() {
        // after the effect the player has the right to an extra pick
        strategy.effect(context);
        assertTrue(currPlayer.deservesExtraPick());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        strategy.effect(context);
        assertFalse(otherPlayer.deservesExtraPick());
    }
}