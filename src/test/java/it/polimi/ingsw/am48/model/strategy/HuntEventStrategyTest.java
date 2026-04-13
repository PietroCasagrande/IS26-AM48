package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntEventStrategyTest {

    private Player p1;
    private Player p2;
    private Player p3;
    private PlayerContext context;
    private HuntEventStrategy strategy;

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

        strategy = new HuntEventStrategy(2, null); // 2 pp per hunter
    }

    private void addHunters(Player player, int count) {
        for (int i = 0; i < count; i++) {
            player.addToTribe(new CharacterCard("H" + i, Era.FIRST, null, CharacterType.HUNTER, 2));
        }
    }

    @Test
    void shouldGiveNothingIfNoHunters() {
        // player senza hunter non riceve nulla
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldGiveOneFoodPerHunter() {
        // 1 cibo per ogni hunter indipendentemente dai pp
        addHunters(p1, 3);
        strategy.effect(context);
        assertEquals(3, p1.getFood());
    }

    @Test
    void shouldGivePPPerHunter() {
        // ppPerPlayer pp per ogni hunter
        addHunters(p1, 3);
        strategy.effect(context);
        assertEquals(6, p1.getPoints()); // 2 pp × 3 hunter
    }

    @Test
    void shouldGiveBothFoodAndPPCorrectly() {
        // cibo e pp vengono assegnati entrambi correttamente
        addHunters(p1, 2);
        strategy.effect(context);
        assertEquals(2, p1.getFood());
        assertEquals(4, p1.getPoints()); // 2 pp × 2 hunter
    }

    @Test
    void shouldNotCountOtherCharacterTypes() {
        // altri tipi di personaggi non vengono contati
        p1.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        p1.addToTribe(new CharacterCard("B1", Era.FIRST, null, CharacterType.BUILDER, 2));
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldAffectAllPlayersIndependently() {
        // ogni player viene valutato indipendentemente
        addHunters(p1, 3);
        addHunters(p2, 1);
        // p3 senza hunter
        strategy.effect(context);
        assertEquals(3, p1.getFood());
        assertEquals(6, p1.getPoints()); // 2 pp × 3 hunter
        assertEquals(1, p2.getFood());
        assertEquals(2, p2.getPoints()); // 2 pp × 1 hunter
        assertEquals(0, p3.getFood());
        assertEquals(0, p3.getPoints());
    }

    @Test
    void shouldStackWithExistingFoodAndPoints() {
        // cibo e pp si sommano a quelli già posseduti
        p1.updateFood(5);
        p1.updatePoints(3);
        addHunters(p1, 2);
        strategy.effect(context);
        assertEquals(7, p1.getFood());  // 5 + 2
        assertEquals(7, p1.getPoints()); // 3 + 4
    }

    @Test
    void shouldScaleCorrectlyWithManyHunters() {
        // verifica che la scala sia corretta con molti hunter
        addHunters(p1, 6);
        strategy.effect(context);
        assertEquals(6, p1.getFood());
        assertEquals(12, p1.getPoints()); // 2 pp × 6 hunter
    }

    @Test
    void shouldWorkWithZeroPPPerHunter() {
        HuntEventStrategy zeroStrategy = new HuntEventStrategy(0, null);
        addHunters(p1, 3);
        zeroStrategy.effect(context);
        assertEquals(3, p1.getFood());
        assertEquals(0, p1.getPoints());
    }
}