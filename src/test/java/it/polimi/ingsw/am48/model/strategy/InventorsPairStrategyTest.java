package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventorsPairStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private InventorsPairStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

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
        assertEquals(3, currPlayer.getFood()); // 1 coppia × 3 cibo
    }

    @Test
    void shouldGiveSixFoodWhenTwoPairsCompleted() {
        // due coppie di artifact diversi
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context);
        assertEquals(6, currPlayer.getFood()); // 2 coppie × 3 cibo
    }

    @Test
    void shouldNotGiveFoodForOddInventorAbovePair() {
        // tre inventor uguali formano solo 1 coppia, il terzo non conta
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood()); // solo 1 coppia × 3 cibo
    }

    @Test
    void shouldNotGiveFoodForAlreadyCompletedPairs() {
        // coppie già presenti al momento dell'acquisizione non contano
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // prima chiamata - 1 coppia, 3 cibo
        currPlayer.addArtifact(Artifact.HOOK); // aggiunta di un inventor che non forma coppia
        strategy.effect(context); // nessuna nuova coppia
        assertEquals(3, currPlayer.getFood()); // rimane 3, nessun cibo aggiunto
    }

    @Test
    void shouldGiveAdditionalFoodWhenNewPairCompletedLater() {
        // prima coppia poi seconda coppia in due momenti diversi
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // prima coppia completata
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context); // seconda coppia completata
        assertEquals(6, currPlayer.getFood()); // 2 coppie × 3 cibo totali
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