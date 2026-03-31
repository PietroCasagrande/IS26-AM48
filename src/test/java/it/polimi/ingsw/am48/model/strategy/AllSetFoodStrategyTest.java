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

class AllSetFoodStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private AllSetFoodStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new AllSetFoodStrategy(null);
    }

    private void addOneOfEachType() {
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("Bu1", Era.FIRST, null, CharacterType.BUILDER, 2));
        currPlayer.addToTribe(new CharacterCard("I1", Era.FIRST, null, CharacterType.INVENTOR, 2));
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("P1", Era.FIRST, null, CharacterType.PICKER, 2));
        currPlayer.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
    }

    @Test
    void shouldGiveNoFoodIfSetNotCompleted() {
        // tribe incompleta - mancano alcuni tipi
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFivefoodWhenFirstSetCompleted() {
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(5, currPlayer.getFood()); // 1 set × 5 cibo
    }

    @Test
    void shouldGiveTenFoodWhenTwoSetsCompleted() {
        addOneOfEachType();
        strategy.effect(context); // primo set completato
        addOneOfEachType();
        strategy.effect(context); // secondo set completato
        assertEquals(10, currPlayer.getFood()); // 2 set × 5 cibo
    }

    @Test
    void shouldNotGiveFoodIfNoNewSetCompleted() {
        addOneOfEachType();
        strategy.effect(context); // primo set completato, 5 cibo
        // aggiungo un personaggio che non completa un nuovo set
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        strategy.effect(context); // nessun nuovo set
        assertEquals(5, currPlayer.getFood()); // rimane 5, nessun cibo aggiunto
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}