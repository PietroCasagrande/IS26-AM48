package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Tester", Totem.RED);
    }

    // CharacterCard.acquire
    @Test
    void characterAcquireShouldAddToTribe() {
        // acquiring a character should add it to the player's tribe
        CharacterCard hunter = new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2);
        hunter.acquire(player);
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    @Test
    void characterAcquireMultipleShouldAccumulate() {
        // acquiring multiple characters of different types should accumulate correctly
        new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(player);
        new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(player);
        new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2).acquire(player);
        assertEquals(2, player.getTribe().countByType(CharacterType.HUNTER));
        assertEquals(1, player.getTribe().countByType(CharacterType.SHAMAN));
        assertEquals(3, player.getTribe().tribeSize());
    }

    @Test
    void characterAcquireShouldNotCostFood() {
        // acquiring a character should not cost any food
        player.updateFood(5);
        new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(player);
        assertEquals(5, player.getFood());
    }


    // BuildingCard.acquire
    @Test
    void buildingAcquireWithEnoughFoodShouldSucceed() {
        // acquiring a building with enough food should subtract the cost and add the building
        player.updateFood(5);
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 3, 5);
        building.acquire(player);
        assertEquals(2, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    @Test
    void buildingAcquireShouldAddBuildingPoints() {
        // acquiring a building should add its prestige points to buildingPoints
        player.updateFood(5);
        new BuildingCard("B1", Era.FIRST, null, 2, 7).acquire(player);
        assertEquals(7, player.getTribe().getBuildingPoints());
    }

    @Test
    void buildingAcquireWithDiscountShouldPayLess() {
        // building discount should reduce the food cost
        player.updateFood(5);
        player.updateBuildingDiscount(2);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(player);
        assertEquals(4, player.getFood()); // 5 - max(0, 3-2) = 4
    }

    @Test
    void buildingAcquireWithFullDiscountShouldPayNothing() {
        // discount greater than cost should result in zero payment
        player.updateFood(5);
        player.updateBuildingDiscount(10);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(player);
        assertEquals(5, player.getFood());
    }

    @Test
    void buildingAcquireWithExactFoodShouldSucceed() {
        // paying the exact food should leaver zero food
        player.updateFood(3);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(player);
        assertEquals(0, player.getFood());
    }

    @Test
    void buildingAcquireWithZeroCostShouldAlwaysSucceed() {
        // a building with zero cost should always be acquirable
        new BuildingCard("B1", Era.FIRST, null, 0, 5).acquire(player);
        assertEquals(0, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    @Test
    void buildingAcquireWithoutEnoughFoodShouldThrow() {
        // not enough food should throw InvalidActionException
        player.updateFood(1);
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 3, 5);
        assertThrows(InvalidActionException.class, () -> building.acquire(player));
    }

    @Test
    void buildingAcquireWithZeroFoodShouldThrow() {
        // zero food with positive cost should throw InvalidActionException
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 3, 5);
        assertThrows(InvalidActionException.class, () -> building.acquire(player));
    }

    @Test
    void buildingAcquireWithDiscountButStillNotEnoughShouldThrow() {
        // partial discount with not enough food should still throw InvalidActionException
        player.updateFood(1);
        player.updateBuildingDiscount(2);
        // costo 5, sconto 2 → costo effettivo 3, ma ho solo 1 cibo
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 5, 5);
        assertThrows(InvalidActionException.class, () -> building.acquire(player));
    }

    @Test
    void buildingAcquireFailureShouldNotModifyPlayer() {
        // failed acquire should not modify player's food or buildings
        player.updateFood(1);
        BuildingCard building = new BuildingCard("B1", Era.FIRST, null, 3, 5);
        assertThrows(InvalidActionException.class, () -> building.acquire(player));
        // verifies that player remains untouched
        assertEquals(1, player.getFood());
        assertEquals(0, player.getTribe().getBuildings().size());
    }


    // EventCard.acquire
    @Test
    void eventAcquireShouldAlwaysThrow() {
        // event card can never be acquired
        EventCard event = new EventCard("E1", Era.FIRST, null, EventType.HUNTER_EVENT);
        assertThrows(InvalidActionException.class, () -> event.acquire(player));
    }
}