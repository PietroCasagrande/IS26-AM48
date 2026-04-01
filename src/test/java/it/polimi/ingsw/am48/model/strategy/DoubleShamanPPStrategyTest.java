package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoubleShamanPPStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private DoubleShamanPPStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new DoubleShamanPPStrategy(null);
    }

    @Test
    void shouldBeFalseBeforeEffect() {
        // il player non ha il raddoppio sciamanico prima dell'effetto
        assertFalse(currPlayer.deservesDoubleShamanPp());
    }

    @Test
    void shouldSetShamanDoublingToTrue() {
        // dopo l'effetto il player guadagna il doppio dei PP negli eventi sciamanici
        strategy.effect(context);
        assertTrue(currPlayer.deservesDoubleShamanPp());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        strategy.effect(context);
        assertFalse(otherPlayer.deservesDoubleShamanPp());
    }
}