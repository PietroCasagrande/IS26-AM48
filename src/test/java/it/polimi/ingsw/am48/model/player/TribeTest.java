package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TribeTest {

    private Tribe tribe;

    // metodo eseguito prima di ogni singolo @Test, ogni metodo utilizza un istanza nuova
    @BeforeEach
    void setUp() {
        tribe = new Tribe();
    }


    // tribeSize() — conta tutti i personaggi
    @Test
    void newTribeShouldHaveZeroSize() {
        // Una tribe appena creata non ha personaggi
        assertEquals(0, tribe.tribeSize());
    }

    @Test
    void tribeSizeShouldCountAllCharacterTypes() {
        // Aggiungo personaggi di tipi diversi, verifico che li conti tutti
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
        assertEquals(3, tribe.tribeSize());
    }


    // countByType() — conta i personaggi di un tipo
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


    // addToTribe(CharacterCard) — aggiunge personaggio
    @Test
    void addCharacterShouldIncreaseSizeAndCount() {
        CharacterCard hunter = new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2);
        tribe.addToTribe(hunter);
        assertEquals(1, tribe.tribeSize());
        assertEquals(1, tribe.countByType(CharacterType.HUNTER));
    }

    @Test
    void addMultipleCharactersShouldAccumulate() {
        tribe.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        tribe.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
    }


    // addToTribe(BuildingCard) — aggiunge edificio
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


    // updateCurrentFood() — aggiunge/toglie cibo
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
        // Nelle regole i PP possono andare sotto zero
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


    // addArtifact() — aggiunge artefatto al set
    @Test
    void addArtifactShouldAdd() {
        tribe.addArtifact(Artifact.ARROW);
        assertTrue(tribe.getArtifacts().containsKey(Artifact.ARROW));
        assertEquals(1, tribe.getArtifacts().size());
    }

    @Test
    void addArtifactShouldNotDuplicate() {
        // È un Set, aggiungere lo stesso artefatto due volte non cambia nulla
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


    // payFood() — paga cibo o perdi PP
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
        // Mancano 3 cibo → -3 × 2 = -6 PP
        assertEquals(0, tribe.getCurrentFood());
        assertEquals(-6, tribe.getCurrentPrestigePoints());
    }

    @Test
    void payFoodWithZeroFoodShouldLoseAllAsPrestige() {
        // 0 cibo, devo pagarne 4 a 2 PP ciascuno → -8 PP
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
        assertEquals(20, tribe.getCurrentPrestigePoints()); // invariato
    }


    // computeTotalEndGameScore() — punteggio finale
    @Test
    void endGameScoreShouldAddBuilderAndBuildingPoints() {
        tribe.updateCurrentPrestigePoints(10);
        tribe.updateBuilderPoints(5);
        tribe.addToTribe(new BuildingCard("B1", Era.FIRST, null, 2, 3));
        // PP base = 10, builder = 5, building = 3 → totale 18
        tribe.computeTotalEndGameScore();
        assertEquals(18, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountArtistPairs() {
        // 5 artisti → 2 coppie → 20 PP
        for (int i = 0; i < 5; i++) {
            tribe.addToTribe(new CharacterCard("A" + i, Era.FIRST, null, CharacterType.ARTIST, 2));
        }
        tribe.computeTotalEndGameScore();
        assertEquals(20, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountArtistPairsIntegerDivision() {
        // 1 artista → 0 coppie → 0 PP
        tribe.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        tribe.computeTotalEndGameScore();
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCountInventorsTimesArtifacts() {
        // 3 inventori × 4 artefatti diversi = 12 PP
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
        // 0 inventori × 5 artefatti = 0 PP
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
        // 3 inventori × 0 artefatti = 0 PP
        for (int i = 0; i < 3; i++) {
            tribe.addToTribe(new CharacterCard("I" + i, Era.FIRST, null, CharacterType.INVENTOR, 2));
        }
        tribe.computeTotalEndGameScore();
        assertEquals(0, tribe.getCurrentPrestigePoints());
    }

    @Test
    void endGameScoreShouldCombineEverything() {
        // Scenario completo come nell'esempio del regolamento
        tribe.updateCurrentPrestigePoints(19);  // punti accumulati durante la partita

        // 5 inventori
        for (int i = 0; i < 5; i++) {
            tribe.addToTribe(new CharacterCard("I" + i, Era.FIRST, null, CharacterType.INVENTOR, 2));
        }
        // 4 artefatti diversi → inventori = 5 × 4 = 20 PP
        tribe.addArtifact(Artifact.ARROW);
        tribe.addArtifact(Artifact.BOWL);
        tribe.addArtifact(Artifact.FLUTE);
        tribe.addArtifact(Artifact.CANOE);

        // 2 costruttori con 3+1 PP → builderPoints = 4
        tribe.updateBuilderPoints(4);

        // 2 edifici con 8+2 PP (buildingPoints lo simuliamo direttamente)
        // Nota: addToTribe(BuildingCard) aggiunge automaticamente a buildingPoints
        tribe.addToTribe(new BuildingCard("E1", Era.FIRST, null, 2, 8));
        tribe.addToTribe(new BuildingCard("E2", Era.SECOND, null, 3, 2));

        // Atteso: 19 + 20 (inventori) + 0 (artisti) + 4 (builder) + 10 (edifici) = 53
        // Nota: l'esempio del regolamento dà 68 perché include anche 15 PP
        // da un edificio speciale — quei 15 PP verrebbero aggiunti dalla strategy
        // dell'edificio tramite l'OnEndGameNotificator, non da computeTotalEndGameScore
        tribe.computeTotalEndGameScore();
        assertEquals(53, tribe.getCurrentPrestigePoints());
    }
}