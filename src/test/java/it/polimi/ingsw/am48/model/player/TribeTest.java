package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TribeTest {

    private Tribe tribe;

    // method run before every single @Test, each method uses a fresh instance
    @BeforeEach
    void setUp() {
        tribe = new Tribe();
    }


    // getTotalCharacters() — counts all characters
    @Test
    void newTribeShouldHaveZeroSize() {
        // A freshly created tribe has no characters
        assertEquals(0, tribe.getTotalCharacters());
    }

    @Test
    void getTotalCharactersShouldCountAllCharacterTypes() {
        // Add characters of different types, verify it counts them all
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
        assertEquals(3, tribe.getTotalCharacters());
    }


    // countByType() — counts the characters of a type
    @Test
    void countByTypeShouldReturnZeroForEmptyTribe() {
        assertEquals(0, tribe.countByType(CharacterType.HUNTER));
    }

    @Test
    void countByTypeShouldReturnCorrectCount() {
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("H2", Era.SECOND, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
        assertEquals(1, tribe.countByType(CharacterType.SHAMAN));
        assertEquals(0, tribe.countByType(CharacterType.BUILDER));
    }


    // addToTribe(CharacterCard) — adds a character
    @Test
    void addCharacterShouldIncreaseSizeAndCount() {
        CharacterCard hunter = new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2);
        tribe.addToTribe(hunter);
        assertEquals(1, tribe.getTotalCharacters());
        assertEquals(1, tribe.countByType(CharacterType.HUNTER));
    }

    @Test
    void addMultipleCharactersShouldAccumulate() {
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
    }


    // addToTribe(BuildingCard) — adds a building
    @Test
    void addBuildingShouldAddToListAndUpdatePoints() {
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 2, 5);
        tribe.addToTribe(building);
        assertEquals(1, tribe.getBuildings().size());
        assertEquals(5, tribe.getBuildingPoints());
    }

    @Test
    void addMultipleBuildingsShouldAccumulatePoints() {
        tribe.addToTribe(new BuildingCard("B1", Era.FIRST, null, 2, 5));
        tribe.addToTribe(new BuildingCard("B2", Era.SECOND, null, 3, 3));
        assertEquals(2, tribe.getBuildings().size());
        assertEquals(8, tribe.getBuildingPoints());
    }


    // updateCurrentFood() — adds/removes food
    @Test
    void updateFoodShouldAdd() {
        tribe.updateCurrentFood(5);
        assertEquals(5, tribe.getCurrentFood());
    }

    @Test
    void updateFoodShouldAccumulate() {
        tribe.updateCurrentFood(3);
        tribe.updateCurrentFood(2);
        assertEquals(5, tribe.getCurrentFood());
    }

    @Test
    void updateFoodShouldSubtract() {
        tribe.updateCurrentFood(5);
        tribe.updateCurrentFood(-2);
        assertEquals(3, tribe.getCurrentFood());
    }


    // updateCurrentPrestigePoints()
    @Test
    void updatePrestigeShouldAdd() {
        tribe.updateCurrentPrestigePoints(10);
        assertEquals(10, tribe.getCurrentPrestigePoints());
    }

    @Test
    void updatePrestigeShouldGoNegative() {
        // In the rules PP can go below zero
        tribe.updateCurrentPrestigePoints(-5);
        assertEquals(-5, tribe.getCurrentPrestigePoints());
    }


    // updateFoodDiscount(), updateBuildingDiscount(),
    // updateShamanStars(), updateBuilderPoints()
    @Test
    void updateFoodDiscountShouldAccumulate() {
        tribe.updateFoodDiscount(1);
        tribe.updateFoodDiscount(2);
        assertEquals(3, tribe.getFoodDiscount());
    }

    @Test
    void updateBuildingDiscountShouldAccumulate() {
        tribe.updateBuildingDiscount(1);
        tribe.updateBuildingDiscount(2);
        assertEquals(3, tribe.getBuildingDiscount());
    }

    @Test
    void updateShamanStarsShouldAccumulate() {
        tribe.updateShamanStars(2);
        tribe.updateShamanStars(1);
        assertEquals(3, tribe.getShamanStars());
    }

    @Test
    void updateBuilderPointsShouldAccumulate() {
        tribe.updateBuilderPoints(3);
        tribe.updateBuilderPoints(1);
        assertEquals(4, tribe.getBuilderPoints());
    }


    // addArtifact() — adds an artifact to the set
    @Test
    void addArtifactShouldAdd() {
        tribe.addArtifact(Artifact.ARROW);
        assertTrue(tribe.getArtifacts().containsKey(Artifact.ARROW));
        assertEquals(1, tribe.getArtifacts().size());
    }

    @Test
    void addArtifactShouldNotDuplicate() {
        // It is a Set, adding the same artifact twice changes nothing
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.ARROW);
        assertEquals(1, tribe.getArtifacts().size());
    }

    @Test
    void addArtifactShouldTrackMultipleDifferent() {
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.BOWL);
        tribe.addArtifact(Artifact.FLUTE);
        assertEquals(3, tribe.getArtifacts().size());
    }


    // payFood() — pay food or lose PP
    @Test
    void payFoodWithEnoughShouldJustSubtract() {
        tribe.updateCurrentFood(5);
        tribe.payFood(3, 2);
        assertEquals(2, tribe.getCurrentFood());
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void payFoodExactAmountShouldLeaveZero() {
        tribe.updateCurrentFood(3);
        tribe.payFood(3, 2);
        assertEquals(0, tribe.getCurrentFood());
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void payFoodWithoutEnoughShouldLosePrestige() {
        tribe.updateCurrentFood(2);
        tribe.payFood(5, 2);
        // Missing 3 food → -3 × 2 = -6 PP
        assertEquals(0, tribe.getCurrentFood());
        assertEquals(-6, tribe.getCurrentPrestigePoints());
    }

    @Test
    void payFoodWithZeroFoodShouldLoseAllAsPrestige() {
        // 0 food, must pay 4 at 2 PP each → -8 PP
        tribe.payFood(4, 2);
        assertEquals(0, tribe.getCurrentFood());
        assertEquals(-8, tribe.getCurrentPrestigePoints());
    }

    @Test
    void payFoodShouldNotAffectPrestigeWhenFoodSufficient() {
        tribe.updateCurrentFood(10);
        tribe.updateCurrentPrestigePoints(20);
        tribe.payFood(5, 3);
        assertEquals(5, tribe.getCurrentFood());
        assertEquals(20, tribe.getCurrentPrestigePoints()); // unchanged
    }


    // computeTotalEndGameScore() — final score
    @Test
    void endGameScoreShouldAddBuilderAndBuildingPoints() {
        tribe.updateCurrentPrestigePoints(10);
        tribe.updateBuilderPoints(5);
        tribe.addToTribe(new BuildingCard("B1", Era.FIRST, null, 2, 3));
        // base PP = 10, builder = 5, building = 3 → total 18
        tribe.computeTotalEndGameScore();
        assertEquals(18, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountArtistPairs() {
        // 5 artists → 2 pairs → 20 PP
        for (int i = 0; i < 5; i++) {
            tribe.addToTribe(new CharacterCard("A" + i, Era.FIRST, null, CharacterType.ARTIST, 2));
        }
        tribe.computeTotalEndGameScore();
        assertEquals(20, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountArtistPairsIntegerDivision() {
        // 1 artist → 0 pairs → 0 PP
        tribe.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        tribe.computeTotalEndGameScore();
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountInventorsTimesArtifacts() {
        // 3 inventors × 4 different artifacts = 12 PP
        for (int i = 0; i < 3; i++) {
            tribe.addToTribe(new CharacterCard("I" + i, Era.FIRST, null, CharacterType.INVENTOR, 2));
        }
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.BOWL);
        tribe.addArtifact(Artifact.FLUTE);
        tribe.addArtifact(Artifact.CANOE);
        tribe.computeTotalEndGameScore();
        assertEquals(12, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreWithZeroInventorsShouldGiveZeroInventorPoints() {
        // 0 inventors × 5 artifacts = 0 PP
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.BOWL);
        tribe.addArtifact(Artifact.FLUTE);
        tribe.addArtifact(Artifact.CANOE);
        tribe.addArtifact(Artifact.DOLL);
        tribe.computeTotalEndGameScore();
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreWithZeroArtifactsShouldGiveZeroInventorPoints() {
        // 3 inventors × 0 artifacts = 0 PP
        for (int i = 0; i < 3; i++) {
            tribe.addToTribe(new CharacterCard("I" + i, Era.FIRST, null, CharacterType.INVENTOR, 2));
        }
        tribe.computeTotalEndGameScore();
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCombineEverything() {
        // Full scenario as in the rulebook example
        tribe.updateCurrentPrestigePoints(19);  // points accumulated during the game

        // 5 inventors
        for (int i = 0; i < 5; i++) {
            tribe.addToTribe(new CharacterCard("I" + i, Era.FIRST, null, CharacterType.INVENTOR, 2));
        }
        // 4 different artifacts → inventors = 5 × 4 = 20 PP
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.BOWL);
        tribe.addArtifact(Artifact.FLUTE);
        tribe.addArtifact(Artifact.CANOE);

        // 2 builders with 3+1 PP → builderPoints = 4
        tribe.updateBuilderPoints(4);

        // 2 buildings with 8+2 PP (we simulate buildingPoints directly)
        // Note: addToTribe(BuildingCard) automatically adds to buildingPoints
        tribe.addToTribe(new BuildingCard("E1", Era.FIRST, null, 2, 8));
        tribe.addToTribe(new BuildingCard("E2", Era.SECOND, null, 3, 2));

        // Expected: 19 + 20 (inventors) + 0 (artists) + 4 (builder) + 10 (buildings) = 53
        // Note: the rulebook example gives 68 because it also includes 15 PP
        // from a special building — those 15 PP would be added by the building's
        // strategy via the OnEndGameNotificator, not by computeTotalEndGameScore
        tribe.computeTotalEndGameScore();
        assertEquals(53, tribe.getCurrentPrestigePoints());
    }
}
