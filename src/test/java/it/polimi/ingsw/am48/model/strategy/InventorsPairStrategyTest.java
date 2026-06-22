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
    private NotificatorCenter mockNotificatorCenter; // mock declaration

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
        // a single inventor with ARROW does not form a pair
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveThreeFoodWhenFirstPairCompleted() {
        // two inventors with the same artifact form a pair
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldGiveSixFoodWhenTwoPairsCompleted() {
        // two pairs of different artifacts grant 6 food in total
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context);
        assertEquals(6, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodForOddInventorAbovePair() {
        // three identical inventors form only 1 pair, the third does not count
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldNotGiveFoodIfNoNewPairCompleted() {
        // form a pair, then add an inventor that does not form a pair
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // completes a pair
        currPlayer.addArtifact(Artifact.HOOK); // does not form a pair
        strategy.effect(context); // no new pair
        assertEquals(3, currPlayer.getFood()); // stays 3
    }

    @Test
    void shouldNotGiveFoodForPairsAlreadyPresentAtAcquisition() {
        // pairs already present before acquisition must not count
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.registerTo(mockNotificatorCenter, context); // simulates acquisition
        strategy.effect(context); // no new pair since acquisition
        assertEquals(0, currPlayer.getFood());
    }

    @Test
    void shouldGiveFoodOnlyForNewPairsAfterAcquisition() {
        // pre-existing pairs do not count, only new ones after acquisition
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.registerTo(mockNotificatorCenter, context); // acquisition with 1 pair already present
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context); // new pair formed after acquisition
        assertEquals(3, currPlayer.getFood()); // only 1 new pair × 3 food
    }

    @Test
    void shouldGiveAdditionalFoodWhenNewPairCompletedLater() {
        // two pairs completed at different times grant food separately
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context); // first pair
        currPlayer.addArtifact(Artifact.HOOK);
        currPlayer.addArtifact(Artifact.HOOK);
        strategy.effect(context); // second pair
        assertEquals(6, currPlayer.getFood());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        // the effect must not modify the other players
        currPlayer.addArtifact(Artifact.ARROW);
        currPlayer.addArtifact(Artifact.ARROW);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}
