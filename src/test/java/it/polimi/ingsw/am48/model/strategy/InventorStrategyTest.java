package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventorStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;
    private InventorStrategy strategy;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);

        strategy = new InventorStrategy(Artifact.ARROW, null, null);
    }

    @Test
    void shouldAddCorrectArtifactToCurrentPlayer() {
        strategy.effect(context);
        assertTrue(currPlayer.getArtifacts().containsKey(Artifact.ARROW));
    }

    @Test
    void shouldAddExactlyOneArtifact() {
        strategy.effect(context);
        assertEquals(1, currPlayer.getArtifacts().size());
    }

    @Test
    void shouldNotAddWrongArtifact() {
        strategy.effect(context);
        assertFalse(currPlayer.getArtifacts().containsKey(Artifact.CANOE));
    }

    @Test
    void shouldNotModifyOtherPlayers() {
        strategy.effect(context);
        assertTrue(otherPlayer.getArtifacts().isEmpty());
    }
}