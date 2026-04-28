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

        strategy = new ShamanEventStrategy(1, 3, null); // ppToMin=1, ppToMax=3
    }

    @Test
    void shouldGivePPToPlayerWithMostStars() {
        // il player con più stelle riceve ppToMax
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());
    }

    @Test
    void shouldLosePPToPlayerWithFewestStars() {
        // i player con meno stelle perdono ppToMin
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }

    @Test
    void shouldGiveNothingToMiddlePlayers() {
        // i player con stelle intermedie non ricevono né perdono punti
        p1.updateShamanStars(3);
        p2.updateShamanStars(2);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(0, p2.getPoints());
    }

    @Test
    void shouldGivePPToAllPlayersWithMaxStars() {
        // più player con stesso massimo ricevono tutti ppToMax
        p1.updateShamanStars(3);
        p2.updateShamanStars(3);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());
        assertEquals(3, p2.getPoints());
    }

    @Test
    void shouldLosePPToAllPlayersWithMinStars() {
        // più player con stesso minimo perdono tutti ppToMin
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }

    @Test
    void shouldApplyBothEffectsWhenAllPlayersHaveSameStars() {
        // tutti con stesse stelle: ricevono ppToMax e perdono ppToMin
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
        // player con doubling riceve ppToMax doppio
        p1.updateShamanStars(3);
        p1.setShamanDoubling();
        p2.updateShamanStars(1);
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(6, p1.getPoints()); // 3 + 3 doppio
    }

    @Test
    void shouldNotLosePPIfShamanSafe() {
        // player con shamanSafety non perde PP pur avendo stelle minime
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p2.setShamanSafety();
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(3, p1.getPoints());  // massimo, riceve ppToMax
        assertEquals(0, p2.getPoints());  // minimo ma immune, non perde
        assertEquals(-1, p3.getPoints()); // minimo e non immune, perde ppToMin

    }

    @Test
    void shouldNotDoublePPForMinPlayers() {
        // il doubling non si applica ai player con stelle minime
        p1.updateShamanStars(3);
        p2.updateShamanStars(1);
        p2.setShamanDoubling(); // doubling non conta per chi perde
        p3.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(-1, p2.getPoints()); // perde normale, doubling non conta
    }

    @Test
    void shouldHandleZeroStarsForAll() {
        // tutti con 0 stelle - stesso effetto di parità generale
        strategy.effect(context);
        assertEquals(2, p1.getPoints()); // 3 - 1
        assertEquals(2, p2.getPoints());
        assertEquals(2, p3.getPoints());
    }

    @Test
    void shouldStackWithExistingPoints() {
        // l'effetto si somma ai punti già posseduti
        p1.updateShamanStars(3);
        p1.updatePoints(5);
        p2.updateShamanStars(1);
        strategy.effect(context);
        assertEquals(8, p1.getPoints()); // 5 + 3
    }
}