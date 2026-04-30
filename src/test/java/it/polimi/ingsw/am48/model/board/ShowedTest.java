package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShowedTest {

    private Showed<Card> showed;
    private Player player;
    private PlayerContext playerContext;

    @BeforeEach
    void setUp() {
        showed = new Showed<>();
        player = new Player("Ilaria", Totem.RED);
        playerContext = new PlayerContext();
        playerContext.addPlayer(player);
        playerContext.setCurrPlayer(player);
    }

    // ==================== addUpperCards / addLowerCards ====================

    @Test
    @DisplayName("addUpperCards: should populate upper list only")
    void shouldPopulateUpperListOnly() {
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertEquals(2, showed.getUpperList().size());
        assertEquals(0, showed.getLowerList().size());
    }

    @Test
    @DisplayName("addLowerCards: should populate lower list only")
    void shouldPopulateLowerListOnly() {
        showed.addLowerCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertEquals(0, showed.getUpperList().size());
        assertEquals(1, showed.getLowerList().size());
    }

    @Test
    @DisplayName("addUpperCards: should accumulate across multiple calls")
    void shouldAccumulateUpperCardsAcrossMultipleCalls() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addUpperCards(List.of(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertEquals(2, showed.getUpperList().size());
    }

    // ==================== getUpperList / getLowerList (immutability) ====================

    @Test
    @DisplayName("getUpperList: should return a defensive copy")
    void shouldReturnDefensiveCopyOfUpperList() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        List<Card> copy = showed.getUpperList();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(mock(Card.class)));
        assertEquals(1, showed.getUpperList().size());
    }

    @Test
    @DisplayName("getLowerList: should return a defensive copy")
    void shouldReturnDefensiveCopyOfLowerList() {
        showed.addLowerCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        List<Card> copy = showed.getLowerList();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(mock(Card.class)));
        assertEquals(1, showed.getLowerList().size());
    }

    // ==================== shiftRow ====================

    @Test
    @DisplayName("shiftRow: should move all upper cards to lower and clear upper")
    void shouldMoveUpperCardsToLowerAndClearUpper() {
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2)));
        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());
    }

    @Test
    @DisplayName("shiftRow: should append upper cards to existing lower cards")
    void shouldAppendUpperCardsToExistingLower() {
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.SECOND, null, CharacterType.HUNTER, 2)));
        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());
    }

    // ==================== clearBottom ====================

    @Test
    @DisplayName("clearBottom: should empty the lower list")
    void shouldEmptyLowerList() {
        showed.addLowerCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.clearBottom();
        assertEquals(0, showed.getLowerList().size());
    }

    @Test
    @DisplayName("clearBottom: should not affect upper list")
    void shouldNotAffectUpperListOnClearBottom() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.clearBottom();
        assertEquals(1, showed.getUpperList().size());
        assertEquals(0, showed.getLowerList().size());
    }

    // ==================== isTop / isDown ====================

    @Test
    @DisplayName("isTop: should return true for a card in the upper list")
    void shouldReturnTrueForUpperCard() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.isTop("H1"));
        assertFalse(showed.isDown("H1"));
    }

    @Test
    @DisplayName("isDown: should return true for a card in the lower list")
    void shouldReturnTrueForLowerCard() {
        showed.addLowerCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.isDown("H1"));
        assertFalse(showed.isTop("H1"));
    }

    @Test
    @DisplayName("isTop/isDown: should both return false for a nonexistent card")
    void shouldReturnFalseForNonexistentCard() {
        assertFalse(showed.isTop("NONEXISTENT"));
        assertFalse(showed.isDown("NONEXISTENT"));
    }

    // ==================== diffLastEras ====================

    @Test
    @DisplayName("diffLastEras: should return false when both lists have the same era")
    void shouldReturnFalseWhenSameEra() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    @Test
    @DisplayName("diffLastEras: should return true when last cards have different eras")
    void shouldReturnTrueWhenDifferentEras() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.SECOND, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.diffLastEras());
    }

    @Test
    @DisplayName("diffLastEras: should return false when upper list is empty")
    void shouldReturnFalseWhenUpperEmpty() {
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    @Test
    @DisplayName("diffLastEras: should return false when lower list is empty")
    void shouldReturnFalseWhenLowerEmpty() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    @Test
    @DisplayName("diffLastEras: should return false when both lists are empty")
    void shouldReturnFalseWhenBothEmpty() {
        assertFalse(showed.diffLastEras());
    }

    // ==================== registerBottom ====================

    @Test
    @DisplayName("registerBottom: should call registerTo on each card's non-null strategy in era order")
    void shouldCallRegisterToOnEachCardStrategyInEraOrder() {
        NotificatorCenter nc = mock(NotificatorCenter.class);
        PlayerContext ctx = mock(PlayerContext.class);
        CardStrategy strategyFirst  = mock(CardStrategy.class);
        CardStrategy strategySecond = mock(CardStrategy.class);

        showed.addLowerCards(List.of(
                new CharacterCard("S1", Era.SECOND, strategySecond, CharacterType.HUNTER, 2),
                new CharacterCard("F1", Era.FIRST,  strategyFirst,  CharacterType.HUNTER, 2)));

        showed.registerBottom(nc, ctx);

        verify(strategyFirst,  times(1)).registerTo(nc, ctx);
        verify(strategySecond, times(1)).registerTo(nc, ctx);
    }

    @Test
    @DisplayName("registerBottom: should skip cards with null strategy without throwing")
    void shouldSkipCardsWithNullStrategy() {
        NotificatorCenter nc = mock(NotificatorCenter.class);
        PlayerContext ctx = mock(PlayerContext.class);
        CardStrategy strategy = mock(CardStrategy.class);

        // One card with strategy, one without
        showed.addLowerCards(List.of(
                new CharacterCard("F1", Era.FIRST,  strategy, CharacterType.HUNTER, 2),
                new CharacterCard("F2", Era.FIRST,  null,     CharacterType.HUNTER, 2)));

        assertDoesNotThrow(() -> showed.registerBottom(nc, ctx));
        verify(strategy, times(1)).registerTo(nc, ctx);
    }

    @Test
    @DisplayName("registerBottom: should not throw when lower list is empty")
    void shouldNotThrowWhenLowerListIsEmpty() {
        NotificatorCenter nc = mock(NotificatorCenter.class);
        PlayerContext ctx = mock(PlayerContext.class);
        assertDoesNotThrow(() -> showed.registerBottom(nc, ctx));
    }

    @Test
    @DisplayName("registerBottom: should call strategies in era-ascending order")
    void shouldCallStrategiesInEraAscendingOrder() {
        NotificatorCenter nc = mock(NotificatorCenter.class);
        PlayerContext ctx = mock(PlayerContext.class);
        CardStrategy strategyFirst = mock(CardStrategy.class);
        CardStrategy strategyThird = mock(CardStrategy.class);

        showed.addLowerCards(List.of(
                new CharacterCard("T1", Era.THIRD, strategyThird, CharacterType.HUNTER, 2),
                new CharacterCard("F1", Era.FIRST, strategyFirst, CharacterType.HUNTER, 2)));

        showed.registerBottom(nc, ctx);

        org.mockito.InOrder inOrder = inOrder(strategyFirst, strategyThird);
        inOrder.verify(strategyFirst).registerTo(nc, ctx);
        inOrder.verify(strategyThird).registerTo(nc, ctx);
    }

    // ==================== takeCard ====================

    @Test
    @DisplayName("takeCard: should remove CharacterCard from upper list and acquire it")
    void shouldRemoveCharacterCardFromUpperAndAcquire() {
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2)));
        Optional<Card> taken = showed.takeCard(playerContext, "H1");
        assertTrue(taken.isPresent());
        assertEquals("H1", taken.get().getCardId());
        assertEquals(1, showed.getUpperList().size());
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    @Test
    @DisplayName("takeCard: should remove CharacterCard from lower list and acquire it")
    void shouldRemoveCharacterCardFromLowerAndAcquire() {
        showed.addLowerCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2)));
        Optional<Card> taken = showed.takeCard(playerContext, "H2");
        assertTrue(taken.isPresent());
        assertEquals(1, showed.getLowerList().size());
        assertEquals(1, player.getTribe().countByType(CharacterType.SHAMAN));
    }

    @Test
    @DisplayName("takeCard: should search upper list before lower list")
    void shouldSearchUpperListBeforeLowerList() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("H1", Era.SECOND, null, CharacterType.HUNTER, 2)));

        Optional<Card> taken = showed.takeCard(playerContext, "H1");

        assertTrue(taken.isPresent());
        assertEquals(0, showed.getUpperList().size());
        assertEquals(1, showed.getLowerList().size());
    }

    @Test
    @DisplayName("takeCard: should return empty Optional when card is not found")
    void shouldReturnEmptyWhenCardNotFound() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        Optional<Card> taken = showed.takeCard(playerContext, "NONEXISTENT");
        assertTrue(taken.isEmpty());
        assertEquals(1, showed.getUpperList().size());
    }

    @Test
    @DisplayName("takeCard: should succeed and remove BuildingCard when player has enough food")
    void shouldSucceedAndRemoveBuildingCardWhenEnoughFood() {
        Showed<BuildingCard> buildingShowed = new Showed<>();
        buildingShowed.addUpperCards(List.of(new BuildingCard("B1", Era.FIRST, null, 3, 5)));
        player.updateFood(5);

        Optional<BuildingCard> taken = buildingShowed.takeCard(playerContext, "B1");

        assertTrue(taken.isPresent());
        assertEquals(0, buildingShowed.getUpperList().size());
        assertEquals(2, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    @Test
    @DisplayName("takeCard: should throw InvalidActionException and leave showed unchanged when food is insufficient")
    void shouldThrowAndLeaveShowedUnchangedWhenInsufficientFood() {
        Showed<BuildingCard> buildingShowed = new Showed<>();
        buildingShowed.addUpperCards(List.of(new BuildingCard("B1", Era.FIRST, null, 3, 5)));
        player.updateFood(1);

        assertThrows(InvalidActionException.class, () -> buildingShowed.takeCard(playerContext, "B1"));
        assertEquals(1, buildingShowed.getUpperList().size());
        assertEquals(1, player.getFood());
        assertEquals(0, player.getTribe().getBuildings().size());
    }

    @Test
    @DisplayName("takeCard: should throw InvalidActionException when trying to acquire an EventCard")
    void shouldThrowWhenTakingEventCard() {
        showed.addUpperCards(List.of(new EventCard("EV1", Era.FIRST, null, EventType.HUNTER_EVENT)));
        assertThrows(InvalidActionException.class, () -> showed.takeCard(playerContext, "EV1"));
        assertEquals(1, showed.getUpperList().size());
    }

    // ==================== full round simulation ====================

    @Test
    @DisplayName("fullRound: should correctly simulate a full turn cycle")
    void shouldCorrectlySimulateFullTurnCycle() {
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER,  2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN,  2),
                new CharacterCard("H3", Era.FIRST, null, CharacterType.BUILDER, 2)));

        showed.takeCard(playerContext, "H2");
        assertEquals(2, showed.getUpperList().size());
        assertEquals(1, player.getTotalCharacters());

        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());

        showed.addUpperCards(List.of(new CharacterCard("H4", Era.SECOND, null, CharacterType.ARTIST, 2)));
        assertEquals(1, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());

        assertTrue(showed.diffLastEras());
    }
}