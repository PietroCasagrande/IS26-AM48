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

        strategy = new ShamanSafetyStrategy(null, null);
    }

    @Test
    void shouldBeFalseBeforeEffect() {
        // il player non è immune agli eventi sciamanici prima dell'effetto
        assertFalse(currPlayer.isShamanSafe());
    }

    @Test
    void shouldSetShamanSafetyToTrue() {
        // dopo l'effetto il player è immune agli eventi sciamanici
        strategy.effect(context);
        assertTrue(currPlayer.isShamanSafe());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        strategy.effect(context);
        assertFalse(otherPlayer.isShamanSafe());
    }
}