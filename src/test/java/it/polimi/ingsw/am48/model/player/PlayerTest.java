package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Sveva", Totem.RED);
    }

    // New player should have correct nickname, totem, and an empty tribe
    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("Sveva", player.getNickname());
        assertEquals(Totem.RED, player.getTotem());
        assertEquals(0, player.getFood());
        assertEquals(0, player.getPoints());
    }

    // Adding a character card should be reflected in player's getters
    @Test
    void addCharacterShouldBeAccessibleFromPlayer() {
        player.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        assertEquals(1, player.getTribe().tribeSize());
    }

    // Adding a building should be reflected in player's buildingPoints
    @Test
    void addBuildingShouldUpdateBuildingPoints() {
        player.addToTribe(new BuildingCard("B1", Era.FIRST, null, 2, 7));
        assertEquals(7, player.getBuildingPoints());
    }

    // payFood with enough food should only subtract food
    @Test
    void payFoodWithEnoughShouldOnlySubtractFood() {
        player.updateFood(5);
        player.payFood(3, 2);
        assertEquals(2, player.getFood());
        assertEquals(0, player.getPoints());
    }

    // payFood without enough food should lose prestige
    @Test
    void payFoodWithoutEnoughShouldLosePrestige() {
        player.updateFood(1);
        player.payFood(4, 2);
        assertEquals(0, player.getFood());
        assertEquals(-6, player.getPoints());
    }
}