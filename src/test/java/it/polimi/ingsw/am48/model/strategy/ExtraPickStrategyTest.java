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
        // il diritto alla pesca extra è false di default
        assertFalse(currPlayer.deservesExtraPick());
    }

    @Test
    void shouldSetExtraPickRightToTrue() {
        // dopo l'effetto il player ha diritto alla pesca extra
        strategy.effect(context);
        assertTrue(currPlayer.deservesExtraPick());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        strategy.effect(context);
        assertFalse(otherPlayer.deservesExtraPick());
    }
}