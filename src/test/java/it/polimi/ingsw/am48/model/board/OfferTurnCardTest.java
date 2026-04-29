package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferTurnCardTest {

    @Mock private Player player1;
    @Mock private Player player2;
    @Mock private Player player3;
    @Mock private Player player4;

    // FOOD_REWARDS: pos1=3(nonzero), pos2=1(nonzero), pos3=0, last=2
    private static final List<Integer> FOOD_REWARDS = List.of(3, 1, 0, 2);
    private static final int PP_PENALTY = 2;
    private static final int NUM_PLAYERS = 4;

    private OfferTurnCard offerTurnCard;

    @BeforeEach
    void setUp() {
        offerTurnCard = new OfferTurnCard(NUM_PLAYERS, FOOD_REWARDS, PP_PENALTY);
    }

    // ==================== constructor ====================

    @Test
    @DisplayName("constructor: should store numPlayers correctly")
    void shouldStoreNumPlayersCorrectly() {
        assertEquals(NUM_PLAYERS, offerTurnCard.getNumPlayers());
    }

    @Test
    @DisplayName("constructor: should store foodRewards correctly")
    void shouldStoreFoodRewardsCorrectly() {
        assertEquals(FOOD_REWARDS, offerTurnCard.getFoodRewards());
    }

    @Test
    @DisplayName("constructor: should store ppPenalty correctly")
    void shouldStorePpPenaltyCorrectly() {
        assertEquals(PP_PENALTY, offerTurnCard.getPpPenalty());
    }

    @Test
    @DisplayName("constructor: order should be empty before setupOrder is called")
    void shouldHaveEmptyOrderBeforeSetup() {
        assertNull(offerTurnCard.getNextTotem());
    }

    // ==================== setupOrder ====================

    @Test
    @DisplayName("setupOrder: should make order non-empty after setup")
    void shouldMakeOrderNonEmptyAfterSetup() {
        offerTurnCard.setupOrder(List.of(player1, player2, player3, player4));
        assertNotNull(offerTurnCard.getNextTotem());
    }

    @Test
    @DisplayName("setupOrder: should call updateFood on every player")
    void shouldCallUpdateFoodOnEveryPlayer() {
        offerTurnCard.setupOrder(List.of(player1, player2, player3, player4));
        verify(player1, times(1)).updateFood(anyInt());
        verify(player2, times(1)).updateFood(anyInt());
        verify(player3, times(1)).updateFood(anyInt());
        verify(player4, times(1)).updateFood(anyInt());
    }

    @Test
    @DisplayName("setupOrder: should assign initial food values from the set {2, 3, 4}")
    void shouldAssignInitialFoodValuesFromExpectedSet() {
        offerTurnCard.setupOrder(List.of(player1, player2, player3, player4));

        List<Integer> validInitialRewards = List.of(2, 3, 4);
        ArgumentCaptor<Integer> cap1 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> cap2 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> cap3 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> cap4 = ArgumentCaptor.forClass(Integer.class);

        verify(player1).updateFood(cap1.capture());
        verify(player2).updateFood(cap2.capture());
        verify(player3).updateFood(cap3.capture());
        verify(player4).updateFood(cap4.capture());

        assertTrue(validInitialRewards.contains(cap1.getValue()));
        assertTrue(validInitialRewards.contains(cap2.getValue()));
        assertTrue(validInitialRewards.contains(cap3.getValue()));
        assertTrue(validInitialRewards.contains(cap4.getValue()));
    }

    @Test
    @DisplayName("setupOrder: should include all four players in the order")
    void shouldIncludeAllPlayersInOrder() {
        List<Player> players = List.of(player1, player2, player3, player4);
        offerTurnCard.setupOrder(players);
        List<Player> placeOrder = offerTurnCard.getPlaceOrder();
        assertTrue(placeOrder.containsAll(players));
        assertEquals(4, placeOrder.size());
    }

    // ==================== getNextTotem ====================

    @Test
    @DisplayName("getNextTotem: should return null when order is empty")
    void shouldReturnNullWhenOrderIsEmpty() {
        assertNull(offerTurnCard.getNextTotem());
    }

    @Test
    @DisplayName("getNextTotem: should return the only player when order has one element")
    void shouldReturnOnlyPlayerWhenOrderHasOneElement() {
        offerTurnCard.setupOrder(List.of(player1));
        assertEquals(player1, offerTurnCard.getNextTotem());
    }

    // ==================== removeNextTotem ====================

    @Test
    @DisplayName("removeNextTotem: should make order empty after removing the only totem")
    void shouldMakeOrderEmptyAfterRemovingOnlyTotem() {
        offerTurnCard.setupOrder(List.of(player1));
        offerTurnCard.removeNextTotem();
        assertNull(offerTurnCard.getNextTotem());
    }

    @Test
    @DisplayName("removeNextTotem: should expose the second player after the first totem is removed")
    void shouldExposeSecondPlayerAfterFirstTotemRemoved() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);

        assertEquals(player1, offerTurnCard.getNextTotem());
        offerTurnCard.removeNextTotem();
        assertEquals(player2, offerTurnCard.getNextTotem());
    }

    // ==================== returnTotem - reset of extraFoodRight ====================

    @Test
    @DisplayName("returnTotem: should always reset extraFoodRight to false before potentially setting it to true")
    void shouldAlwaysResetExtraFoodRightBeforeAssigning() {
        offerTurnCard.returnTotem(player1);
        InOrder inOrder = inOrder(player1);
        inOrder.verify(player1).setExtraFoodRight(false);
        inOrder.verify(player1).setExtraFoodRight(true);
    }

    // ==================== returnTotem - position 1 ====================

    @Test
    @DisplayName("returnTotem: should give food reward to player in first position")
    void shouldGiveFoodRewardToFirstPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        verify(player1).updateFood(FOOD_REWARDS.get(0));
    }

    @Test
    @DisplayName("returnTotem: should grant extraFoodRight to first position player when reward is non-zero")
    void shouldGrantExtraFoodRightToFirstPositionWhenRewardNonZero() {
        offerTurnCard.returnTotem(player1); // FOOD_REWARDS.get(0) = 3 ≠ 0
        verify(player1).setExtraFoodRight(true);
    }

    @Test
    @DisplayName("returnTotem: should not apply payFood to first position player")
    void shouldNotApplyPayFoodToFirstPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        verify(player1, never()).payFood(anyInt(), anyInt());
    }

    // ==================== returnTotem - position 2 ====================

    @Test
    @DisplayName("returnTotem: should give food reward to player in second position")
    void shouldGiveFoodRewardToSecondPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        verify(player2).updateFood(FOOD_REWARDS.get(1));
    }

    @Test
    @DisplayName("returnTotem: should grant extraFoodRight to second position player when reward is non-zero")
    void shouldGrantExtraFoodRightToSecondPositionWhenRewardNonZero() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2); // FOOD_REWARDS.get(1) = 1 ≠ 0
        verify(player2).setExtraFoodRight(true);
    }

    // ==================== returnTotem - middle position (>2, not last) ====================

    @Test
    @DisplayName("returnTotem: should not give food to player in third position")
    void shouldNotGiveFoodToThirdPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).updateFood(anyInt());
    }

    @Test
    @DisplayName("returnTotem: should not grant extraFoodRight to third position player")
    void shouldNotGrantExtraFoodRightToThirdPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).setExtraFoodRight(true);
    }

    @Test
    @DisplayName("returnTotem: should not apply payFood to third position player")
    void shouldNotApplyPayFoodToThirdPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).payFood(anyInt(), anyInt());
    }

    // ==================== returnTotem - last position ====================

    @Test
    @DisplayName("returnTotem: should apply payFood to last player with correct arguments")
    void shouldApplyPayFoodToLastPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        offerTurnCard.returnTotem(player4); // position == numPlayers
        verify(player4).payFood(FOOD_REWARDS.getLast(), PP_PENALTY);
    }

    @Test
    @DisplayName("returnTotem: should not apply payFood to any player before last position")
    void shouldNotApplyPayFoodBeforeLastPosition() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player1, never()).payFood(anyInt(), anyInt());
        verify(player2, never()).payFood(anyInt(), anyInt());
        verify(player3, never()).payFood(anyInt(), anyInt());
    }

    // ==================== returnTotem - zero reward edge case ====================

    @Test
    @DisplayName("returnTotem: should not grant extraFoodRight when position 1 reward is zero")
    void shouldNotGrantExtraFoodRightWhenPositionOneRewardIsZero() {
        OfferTurnCard zeroRewardCard = new OfferTurnCard(NUM_PLAYERS, List.of(0, 0, 0, 0), PP_PENALTY);
        zeroRewardCard.returnTotem(player1);
        verify(player1).setExtraFoodRight(false);
        verify(player1, never()).setExtraFoodRight(true);
    }

    @Test
    @DisplayName("returnTotem: should not grant extraFoodRight when position 2 reward is zero")
    void shouldNotGrantExtraFoodRightWhenPositionTwoRewardIsZero() {
        OfferTurnCard zeroRewardCard = new OfferTurnCard(NUM_PLAYERS, List.of(0, 0, 0, 0), PP_PENALTY);
        zeroRewardCard.returnTotem(player1);
        zeroRewardCard.returnTotem(player2);
        verify(player2).setExtraFoodRight(false);
        verify(player2, never()).setExtraFoodRight(true);
    }

    // ==================== getPlaceOrder ====================

    @Test
    @DisplayName("getPlaceOrder: should return empty list before any setup")
    void shouldReturnEmptyListBeforeSetup() {
        assertTrue(offerTurnCard.getPlaceOrder().isEmpty());
    }

    @Test
    @DisplayName("getPlaceOrder: should return all players after returnTotem calls")
    void shouldReturnAllPlayersAfterReturnTotem() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        List<Player> order = offerTurnCard.getPlaceOrder();
        assertEquals(List.of(player1, player2), order);
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a non-null snapshot")
    void shouldReturnNonNullSnapshot() {
        assertNotNull(offerTurnCard.toSnapshot());
    }

    @Test
    @DisplayName("toSnapshot: should return snapshot with empty totemOrder when order is empty")
    void shouldReturnSnapshotWithEmptyTotemOrderWhenOrderIsEmpty() {
        OfferTurnCardSnapshot snapshot = offerTurnCard.toSnapshot();
        assertTrue(snapshot.getTotemOrder().isEmpty());
    }

    @Test
    @DisplayName("toSnapshot: should return snapshot with correct nicknames in order")
    void shouldReturnSnapshotWithCorrectTotemNamesInOrder() {
        when(player1.getNickname()).thenReturn("Alice");
        when(player2.getNickname()).thenReturn("Bob");

        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);

        OfferTurnCardSnapshot snapshot = offerTurnCard.toSnapshot();

        assertEquals(List.of("Alice", "Bob"), snapshot.getTotemOrder());
    }

    @Test
    @DisplayName("toSnapshot: should reflect order changes after removeNextTotem")
    void shouldReflectOrderAfterRemoveNextTotem() {
        when(player2.getNickname()).thenReturn("Bob");

        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.removeNextTotem(); // removes player1

        OfferTurnCardSnapshot snapshot = offerTurnCard.toSnapshot();

        assertEquals(List.of("Bob"), snapshot.getTotemOrder());
    }
}