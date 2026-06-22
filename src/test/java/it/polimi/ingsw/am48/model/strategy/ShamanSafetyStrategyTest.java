package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShamanSafetyStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private ShamanSafetyStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new ShamanSafetyStrategy(null);
    }

    @Test
    void shouldBeFalseBeforeEffect() {
        // the player is not immune to shaman events before the effect
        assertFalse(currPlayer.isShamanSafe());
    }

    @Test
    void shouldSetShamanSafetyToTrue() {
        // after the effect the player is immune to shaman events
        strategy.effect(context);
        assertTrue(currPlayer.isShamanSafe());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        strategy.effect(context);
        assertFalse(otherPlayer.isShamanSafe());
    }
}