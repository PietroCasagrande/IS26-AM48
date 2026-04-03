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

class SustenanceStrategyTest {

    private Player p1;
    private Player p2;
    private PlayerContext context;
    private SustenanceStrategy strategy;

    @BeforeEach
    void setUp() {
        p1 = new Player("alice", Totem.BLACK);
        p2 = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(p1);
        context.addPlayer(p1);
        context.addPlayer(p2);

        strategy = new SustenanceStrategy(1, null, null); // 1 pp perso per ogni cibo mancante
    }

    private void addCharacters(Player player, int count) {
        for (int i = 0; i < count; i++) {
            player.addToTribe(new CharacterCard("H" + i, Era.FIRST, null, CharacterType.HUNTER, 2));
        }
    }

    @Test
    void shouldPayNoFoodIfNoCharacters() {
        // player senza personaggi non paga cibo
        p1.updateFood(3);
        strategy.effect(context);
        assertEquals(3, p1.getFood());
    }

    @Test
    void shouldPayFoodEqualToCharacterCount() {
        // player paga 1 cibo per ogni personaggio
        p1.updateFood(5);
        addCharacters(p1, 3);
        strategy.effect(context);
        assertEquals(2, p1.getFood()); // 5 - 3 = 2
    }

    @Test
    void shouldPayExactlyAllFoodIfEqual() {
        // cibo esattamente uguale al numero di personaggi - arriva a 0 senza perdere PP
        p1.updateFood(3);
        addCharacters(p1, 3);
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldLosePPIfFoodInsufficient() {
        // cibo insufficiente - perde ppLostPerChar per ogni cibo mancante
        p1.updateFood(1);
        addCharacters(p1, 3); // deve pagare 3, ha solo 1
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(-2, p1.getPoints()); // mancano 2 cibo × 1 pp
    }

    @Test
    void shouldLoseAllFoodAndAllPPIfNoFood() {
        // player senza cibo perde pp per ogni personaggio
        addCharacters(p1, 3);
        strategy.effect(context); // ha 0 cibo, deve pagare 3
        assertEquals(0, p1.getFood());
        assertEquals(-3, p1.getPoints()); // 3 cibo mancanti × 1 pp
    }

    @Test
    void shouldApplyFoodDiscountBeforePaying() {
        // foodDiscount riduce il cibo da pagare
        p1.updateFood(2);
        p1.updateFoodDiscount(2); // sconto di 2
        addCharacters(p1, 4); // deve pagare 4 - 2 = 2
        strategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(0, p1.getPoints()); // cibo sufficiente dopo sconto
    }

    @Test
    void shouldNotPayIfDiscountCoversAll() {
        // foodDiscount copre tutti i personaggi, nessun cibo pagato
        p1.updateFood(3);
        p1.updateFoodDiscount(5);
        addCharacters(p1, 3); // deve pagare 3 - 5 = negativo → 0
        strategy.effect(context);
        assertEquals(3, p1.getFood()); // nessun cibo pagato
        assertEquals(0, p1.getPoints());
    }

    @Test
    void shouldAffectAllPlayersIndependently() {
        // ogni player viene valutato indipendentemente
        p1.updateFood(5);
        addCharacters(p1, 2); // p1 paga 2, gliene rimangono 3
        p2.updateFood(1);
        addCharacters(p2, 3); // p2 paga 3, ha solo 1, perde 2 pp
        strategy.effect(context);
        assertEquals(3, p1.getFood());
        assertEquals(0, p1.getPoints());
        assertEquals(0, p2.getFood());
        assertEquals(-2, p2.getPoints());
    }

    @Test
    void shouldLoseMorePPWithHigherPpLostPerChar() {
        // con ppLostPerChar più alto perde più PP per ogni cibo mancante
        SustenanceStrategy heavyStrategy = new SustenanceStrategy(3, null, null);
        addCharacters(p1, 3); // deve pagare 3, ha 0 cibo
        heavyStrategy.effect(context);
        assertEquals(0, p1.getFood());
        assertEquals(-9, p1.getPoints()); // 3 cibo mancanti × 3 pp
    }
}