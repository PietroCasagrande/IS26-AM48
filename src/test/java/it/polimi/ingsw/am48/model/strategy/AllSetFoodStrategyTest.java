package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AllSetFoodStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private AllSetFoodStrategy strategy;
    private NotificatorCenter mockNotificatorCenter; // dichiarazione mock

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        mockNotificatorCenter = mock(NotificatorCenter.class);
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
    void shouldGiveFiveFoodWhenFirstSetCompleted() {
        // un set completo di 6 tipi diversi assegna 5 cibo
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(5, currPlayer.getFood());
    }

    @Test
    void shouldGiveTenFoodWhenTwoSetsCompleted() {
        // due set completati in momenti diversi assegnano 10 cibo totali
        addOneOfEachType();
        strategy.effect(context); // primo set
        addOneOfEachType();
        strategy.effect(context); // secondo set
        assertEquals(10, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodIfNoNewSetCompleted() {
        // aggiungere un personaggio di tipo già presente non completa un nuovo set
        addOneOfEachType();
        strategy.effect(context); // primo set completato, 5 cibo
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        strategy.effect(context); // nessun nuovo set
        assertEquals(5, currPlayer.getFood()); // rimane 5
    }

    @Test
    void shouldNotGiveFoodForSetsAlreadyPresentAtAcquisition() {
        // set già presenti prima dell'acquisizione non devono contare
        addOneOfEachType();
        strategy.registerTo(mockNotificatorCenter, context); // simula acquisizione
        strategy.effect(context); // nessun nuovo set dall'acquisizione
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFoodOnlyForNewSetsAfterAcquisition() {
        // set pre-esistenti non contano, solo quelli nuovi dopo acquisizione
        addOneOfEachType();
        strategy.registerTo(mockNotificatorCenter, context); // acquisizione con 1 set già presente
        addOneOfEachType();
        strategy.effect(context); // nuovo set completato dopo acquisizione
        assertEquals(5, currPlayer.getFood()); // solo 1 nuovo set × 5 cibo
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        addOneOfEachType();
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}