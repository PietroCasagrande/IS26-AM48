package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardTest {

    private Player player;
    private PlayerContext playerContext;

    @BeforeEach
    void setUp() {
        player = new Player("Tester", Totem.RED);
        playerContext = new PlayerContext();
        playerContext.addPlayer(player);
        playerContext.setCurrPlayer(player);
    }

    // ==================== CharacterCard.acquire ====================

    @Test
    @DisplayName("CharacterCard.acquire: should add card to player's tribe")
    void shouldAddCharacterToTribe() {
        CharacterCard hunter = new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2);
        hunter.acquire(playerContext);
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    @Test
    @DisplayName("CharacterCard.acquire: should accumulate multiple characters correctly")
    void shouldAccumulateMultipleCharacters() {
        new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(playerContext);
        new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(playerContext);
        new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2).acquire(playerContext);
        assertEquals(2, player.getTribe().countByType(CharacterType.HUNTER));
        assertEquals(1, player.getTribe().countByType(CharacterType.SHAMAN));
        assertEquals(3, player.getTotalCharacters());
    }

    @Test
    @DisplayName("CharacterCard.acquire: should not cost any food")
    void shouldNotCostFoodWhenAcquiringCharacter() {
        player.updateFood(5);
        new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2).acquire(playerContext);
        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("CharacterCard.acquire: should call strategy.effect when strategy is not null")
    void shouldCallStrategyEffectWhenStrategyIsNotNull() {
        CardStrategy mockStrategy = mock(CardStrategy.class);
        CharacterCard card = new CharacterCard("H1", Era.FIRST, mockStrategy, CharacterType.HUNTER, 2);
        card.acquire(playerContext);
        verify(mockStrategy, times(1)).effect(playerContext);
    }

    @Test
    @DisplayName("CharacterCard.acquire: should not call strategy.effect when strategy is null")
    void shouldNotCallStrategyEffectWhenStrategyIsNull() {
        // No exception should be thrown and card should still be added to tribe
        CharacterCard card = new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2);
        assertDoesNotThrow(() -> card.acquire(playerContext));
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    // ==================== BuildingCard.acquire ====================

    @Test
    @DisplayName("BuildingCard.acquire: should subtract food cost and add building to tribe")
    void shouldSubtractFoodAndAddBuilding() {
        player.updateFood(5);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext);
        assertEquals(2, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should add prestige points to building points")
    void shouldAddBuildingPoints() {
        player.updateFood(5);
        new BuildingCard("B1", Era.FIRST, null, 2, 7).acquire(playerContext);
        assertEquals(7, player.getTribe().getBuildingPoints());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should apply building discount to reduce food cost")
    void shouldApplyBuildingDiscount() {
        player.updateFood(5);
        player.updateBuildingDiscount(2);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext);
        assertEquals(4, player.getFood());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should not cost food when discount covers full cost")
    void shouldPayNothingWhenDiscountCoversFullCost() {
        player.updateFood(5);
        player.updateBuildingDiscount(10);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext);
        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should succeed and leave zero food when paying exact cost")
    void shouldSucceedWithExactFood() {
        player.updateFood(3);
        new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext);
        assertEquals(0, player.getFood());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should always succeed when cost is zero")
    void shouldAlwaysSucceedWithZeroCost() {
        new BuildingCard("B1", Era.FIRST, null, 0, 5).acquire(playerContext);
        assertEquals(0, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    @Test
    @DisplayName("BuildingCard.acquire: should throw InvalidActionException when food is insufficient")
    void shouldThrowWhenFoodIsInsufficient() {
        player.updateFood(1);
        assertThrows(InvalidActionException.class,
                () -> new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext));
    }

    @Test
    @DisplayName("BuildingCard.acquire: should throw InvalidActionException when player has zero food")
    void shouldThrowWhenFoodIsZero() {
        assertThrows(InvalidActionException.class,
                () -> new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext));
    }

    @Test
    @DisplayName("BuildingCard.acquire: should throw when discount is partial and food is still insufficient")
    void shouldThrowWhenDiscountIsPartialAndFoodInsufficient() {
        player.updateFood(1);
        player.updateBuildingDiscount(2);
        assertThrows(InvalidActionException.class,
                () -> new BuildingCard("B1", Era.FIRST, null, 5, 5).acquire(playerContext));
    }

    @Test
    @DisplayName("BuildingCard.acquire: should not modify player state when acquisition fails")
    void shouldNotModifyPlayerWhenAcquisitionFails() {
        player.updateFood(1);
        assertThrows(InvalidActionException.class,
                () -> new BuildingCard("B1", Era.FIRST, null, 3, 5).acquire(playerContext));
        assertEquals(1, player.getFood());
        assertEquals(0, player.getTribe().getBuildings().size());
    }

    // ==================== EventCard.acquire ====================

    @Test
    @DisplayName("EventCard.acquire: should always throw InvalidActionException")
    void shouldAlwaysThrowWhenAcquiringEventCard() {
        EventCard event = new EventCard("E1", Era.FIRST, null, EventType.HUNTER_EVENT);
        assertThrows(InvalidActionException.class, () -> event.acquire(playerContext));
    }
}