package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShamanEventStrategyTest {

    private Player p1;
    private Player p2;
    private Player p3;
    private PlayerContext context;
    private ShamanEventStrategy strategy;

    @BeforeEach
    void setUp() {
        p1 = new Player("alice", Totem.BLACK);
        p2 = new Player("bob", Totem.BLUE);
        p3 = new Player("charlie", Totem.RED);

        context = new PlayerContext();
        context.setCurrPlayer(p1);
        context.addPlayer(p1);
        context.addPlayer(p2);
        context.addPlayer(p3);

        strategy = new ShamanEventStrategy(-1, 3, null); // ppToMin=1, ppToMax=3
    }

    @Test
    void shouldGivePPToPlayerWithMostStars() {
        // the player with the most stars receives ppToMax
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());
    }

    @Test
    void shouldLosePPToPlayerWithFewestStars() {
        // the players with the fewest stars lose ppToMin
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }

    @Test
    void shouldGiveNothingToMiddlePlayers() {
        // players with intermediate stars neither gain nor lose points
        p1.updateShamanStars(3);
        p2.updateShamanStars(2);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(0, p2.getPoints());
    }

    @Test
    void shouldGivePPToAllPlayersWithMaxStars() {
        // multiple players sharing the maximum all receive ppToMax
        p1.updateShamanStars(3);
        p2.updateShamanStars(3);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());
        assertEquals(3, p2.getPoints());
    }

    @Test
    void shouldLosePPToAllPlayersWithMinStars() {
        // multiple players sharing the minimum all lose ppToMin
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }

    @Test
    void shouldApplyBothEffectsWhenAllPlayersHaveSameStars() {
        // everyone with the same stars: they receive ppToMax and lose ppToMin
        p1.updateShamanStars(2);
        p2.updateShamanStars(2);
        p3.updateShamanStars(2);
        strategy.effect(context);
        assertEquals(2, p1.getPoints()); // 3 - 1
        assertEquals(2, p2.getPoints());
        assertEquals(2, p3.getPoints());
    }

    @Test
    void shouldDoubleMaxPPIfDeservesDoubleShamanPp() {
        // player with doubling receives double ppToMax
        p1.updateShamanStars(3);
        p1.setShamanDoubling();
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(6, p1.getPoints()); // 3 + 3 doubled
    }

    @Test
    void shouldNotLosePPIfShamanSafe() {
        // player with shamanSafety does not lose PP even with the minimum stars
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p2.setShamanSafety();
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());  // maximum, receives ppToMax
        assertEquals(0, p2.getPoints());  // minimum but immune, does not lose
        assertEquals(-1, p3.getPoints()); // minimum and not immune, loses ppToMin

    }

    @Test
    void shouldNotDoublePPForMinPlayers() {
        // doubling does not apply to players with the minimum stars
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p2.setShamanDoubling(); // doubling does not count for those who lose
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints()); // loses normally, doubling does not count
    }

    @Test
    void shouldHandleZeroStarsForAll() {
        // everyone with 0 stars - same effect as a general tie
        strategy.effect(context);
        assertEquals(2, p1.getPoints()); // 3 - 1
        assertEquals(2, p2.getPoints());
        assertEquals(2, p3.getPoints());
    }

    @Test
    void shouldStackWithExistingPoints() {
        // the effect adds to the points already held
        p1.updateShamanStars(3);
        p1.updatePoints(5);
        p2.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(8, p1.getPoints()); // 5 + 3
    }
}
