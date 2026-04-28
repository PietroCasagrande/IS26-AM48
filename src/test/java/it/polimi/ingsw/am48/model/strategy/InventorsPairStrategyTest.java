package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class InventorsPairStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private InventorsPairStrategy strategy;
    private NotificatorCenter mockNotificatorCenter; // dichiarazione del mock

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        mockNotificatorCenter = mock(NotificatorCenter.class);
        strategy = new InventorsPairStrategy(null);
    }

    @Test
    void shouldGiveNoFoodIfNoPairCompleted() {
        // un solo inventor con ARROW non forma coppia
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveThreeFoodWhenFirstPairCompleted() {
        // due inventor con lo stesso artifact formano una coppia
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldGiveSixFoodWhenTwoPairsCompleted() {
        // due coppie di artifact diversi assegnano 6 cibo totali
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context);
        assertEquals(6, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodForOddInventorAbovePair() {
        // tre inventor uguali formano solo 1 coppia, il terzo non conta
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodIfNoNewPairCompleted() {
        // formazione coppia, poi inserimento di inventor che non forma coppia
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // completa una coppia
        currPlayer.addArtifact(Artifact.HOOK); // non forma coppia
        strategy.effect(context); // nessuna nuova coppia
        assertEquals(3, currPlayer.getFood()); // rimane 3
    }

    @Test
    void shouldNotGiveFoodForPairsAlreadyPresentAtAcquisition() {
        // coppie già presenti prima dell'acquisizione non devono contare
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.registerTo(mockNotificatorCenter, context); // simula acquisizione
        strategy.effect(context); // nessuna nuova coppia dall'acquisizione
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFoodOnlyForNewPairsAfterAcquisition() {
        // coppie pre-esistenti non contano, solo quelle nuove dopo acquisizione
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.registerTo(mockNotificatorCenter, context); // acquisizione con 1 coppia già presente
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context); // nuova coppia formata dopo acquisizione
        assertEquals(3, currPlayer.getFood()); // solo 1 nuova coppia × 3 cibo
    }

    @Test
    void shouldGiveAdditionalFoodWhenNewPairCompletedLater() {
        // due coppie completate in momenti diversi assegnano cibo separatamente
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // prima coppia
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context); // seconda coppia
        assertEquals(6, currPlayer.getFood());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // l'effetto non deve modificare gli altri giocatori
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}