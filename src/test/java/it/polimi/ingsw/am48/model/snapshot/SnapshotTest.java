package it.polimi.ingsw.am48.model.snapshot;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotTest {
    @Test
    void tribeSnapshotShouldReflectCurrentState() {
        Tribe tribe = new Tribe();
        tribe.updateCurrentFood(5);
        tribe.updateCurrentPrestigePoints(10);
        tribe.addArtifact(Artifact.ARROW);
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));

        TribeSnapshot snapshot = tribe.toSnapshot();

        assertEquals(5, snapshot.getFood());
        assertEquals(10, snapshot.getCurrentPrestigePoints());
        assertTrue(snapshot.getArtifacts().containsKey("ARROW"));
        assertEquals(1, snapshot.getCharacterCardIds().size());
        assertEquals("H1", snapshot.getCharacterCardIds().getFirst());
    }
}
