package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class InventorStrategyTest {

    private Player player;
    private InventorStrategy strategy;

    @BeforeEach
    void setUp() {
        player = new Player("alice", Totem.BLACK);
        strategy = new InventorStrategy(null, Artifact.ARROW);
    }

    @Test
    void shouldAddCorrectArtifactToTribe() {
        strategy.effect(player, List.of());
        assertTrue(player.getTribe().getArtifacts().contains(Artifact.ARROW));
    }

    @Test
    void shouldAddExactlyOneArtifact() {
        strategy.effect(player, List.of());
        assertEquals(1, player.getTribe().getArtifacts().size());
    }

    @Test
    void shouldNotAddWrongArtifact() {
        strategy.effect(player, List.of());
        assertFalse(player.getTribe().getArtifacts().contains(Artifact.CANOE));
    }

    @Test
    void shouldNotModifyOtherPlayers() {
        Player other = new Player("bob", Totem.BLUE);
        strategy.effect(player, List.of(other));
        assertTrue(other.getTribe().getArtifacts().isEmpty());
    }
}