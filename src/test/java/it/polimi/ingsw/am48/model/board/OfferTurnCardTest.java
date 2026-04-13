package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import org.junit.jupiter.api.BeforeEach;
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

    // FOOD_REWARDS.get(0)=3 ≠ 0  →  extraFoodRight=true for pos 1
    // FOOD_REWARDS.get(1)=1 ≠ 0  →  extraFoodRight=true for pos 2
    // FOOD_REWARDS.get(2)=0       →  used in zero-reward tests
    // FOOD_REWARDS.getLast()=2    →  payFood amount for last player
    private static final List<Integer> FOOD_REWARDS = List.of(3, 1, 0, 2);
    private static final int PP_PENALTY = 2;
    private static final int NUM_PLAYERS = 4;

    private OfferTurnCard offerTurnCard;

    @BeforeEach
    void setUp() {
        offerTurnCard = new OfferTurnCard(NUM_PLAYERS, FOOD_REWARDS, PP_PENALTY);
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Test
    void shouldStoreNumPlayersCorrectly() {
        assertEquals(NUM_PLAYERS, offerTurnCard.getNumPlayers());
    }

    @Test
    void shouldStoreFoodRewardsCorrectly() {
        assertEquals(FOOD_REWARDS, offerTurnCard.getFoodRewards());
    }

    @Test
    void shouldStorePpPenaltyCorrectly() {
        assertEquals(PP_PENALTY, offerTurnCard.getPpPenalty());
    }

    // -------------------------------------------------------------------------
    // setupOrder
    // -------------------------------------------------------------------------

    @Test
    void shouldMakeOrderNonEmptyAfterSetup() {
        offerTurnCard.setupOrder(List.of(player1, player2, player3, player4));
        assertNotNull(offerTurnCard.getNextTotem());
    }

    @Test
    void shouldCallUpdateFoodOnEveryPlayerOnSetup() {
        offerTurnCard.setupOrder(List.of(player1, player2, player3, player4));
        verify(player1, times(1)).updateFood(anyInt());
        verify(player2, times(1)).updateFood(anyInt());
        verify(player3, times(1)).updateFood(anyInt());
        verify(player4, times(1)).updateFood(anyInt());
    }

    @Test
    void shouldAssignInitialFoodValuesFromExpectedSet() {
        List<Player> players = List.of(player1, player2, player3, player4);
        offerTurnCard.setupOrder(players);

        // Capture the value given to each player and assert it belongs to [2, 3, 3, 4, 4]
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

    // -------------------------------------------------------------------------
    // getNextTotem
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNullWhenOrderIsEmpty() {
        assertNull(offerTurnCard.getNextTotem());
    }

    @Test
    void shouldReturnOnlyPlayerWhenOrderHasOneElement() {
        // Single player: shuffle is identity → deterministic
        offerTurnCard.setupOrder(List.of(player1));
        assertEquals(player1, offerTurnCard.getNextTotem());
    }

    // -------------------------------------------------------------------------
    // removeNextTotem
    // -------------------------------------------------------------------------

    @Test
    void shouldMakeOrderEmptyAfterRemovingOnlyTotem() {
        offerTurnCard.setupOrder(List.of(player1));
        offerTurnCard.removeNextTotem();
        assertNull(offerTurnCard.getNextTotem());
    }

    @Test
    void shouldExposeSecondPlayerAfterFirstTotemIsRemoved() {
        // Use returnTotem to populate order deterministically (avoids shuffle)
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);

        assertEquals(player1, offerTurnCard.getNextTotem());
        offerTurnCard.removeNextTotem();
        assertEquals(player2, offerTurnCard.getNextTotem());
    }

    // -------------------------------------------------------------------------
    // returnTotem – reset of extraFoodRight
    // -------------------------------------------------------------------------

    @Test
    void shouldAlwaysResetExtraFoodRightToFalseBeforeAssigningIt() {
        offerTurnCard.returnTotem(player1);
        InOrder inOrder = inOrder(player1);
        inOrder.verify(player1).setExtraFoodRight(false);
        inOrder.verify(player1).setExtraFoodRight(true);
    }

    // -------------------------------------------------------------------------
    // returnTotem – position 1
    // -------------------------------------------------------------------------

    @Test
    void shouldGiveFoodRewardToPlayerInFirstPosition() {
        offerTurnCard.returnTotem(player1);
        verify(player1).updateFood(FOOD_REWARDS.get(0));
    }

    @Test
    void shouldGrantExtraFoodRightToFirstPositionWhenRewardIsNonZero() {
        // FOOD_REWARDS.get(0) = 3 ≠ 0
        offerTurnCard.returnTotem(player1);
        verify(player1).setExtraFoodRight(true);
    }

    @Test
    void shouldNotApplyPayFoodToFirstPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        verify(player1, never()).payFood(anyInt(), anyInt());
    }

    // -------------------------------------------------------------------------
    // returnTotem – position 2
    // -------------------------------------------------------------------------

    @Test
    void shouldGiveFoodRewardToPlayerInSecondPosition() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        verify(player2).updateFood(FOOD_REWARDS.get(1));
    }

    @Test
    void shouldGrantExtraFoodRightToSecondPositionWhenRewardIsNonZero() {
        // FOOD_REWARDS.get(1) = 1 ≠ 0
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        verify(player2).setExtraFoodRight(true);
    }

    // -------------------------------------------------------------------------
    // returnTotem – middle position (> 2, not last)
    // -------------------------------------------------------------------------

    @Test
    void shouldNotGiveFoodToPlayerInThirdPosition() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).updateFood(anyInt());
    }

    @Test
    void shouldNotGrantExtraFoodRightToThirdPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).setExtraFoodRight(true);
    }

    @Test
    void shouldNotApplyPayFoodToThirdPositionPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player3, never()).payFood(anyInt(), anyInt());
    }

    // -------------------------------------------------------------------------
    // returnTotem – last position (position == numPlayers)
    // -------------------------------------------------------------------------

    @Test
    void shouldApplyPayFoodToLastPlayer() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        offerTurnCard.returnTotem(player4); // position == numPlayers
        verify(player4).payFood(FOOD_REWARDS.getLast(), PP_PENALTY);
    }

    @Test
    void shouldNotApplyPayFoodToAnyPlayerBeforeLastPosition() {
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);
        offerTurnCard.returnTotem(player3);
        verify(player1, never()).payFood(anyInt(), anyInt());
        verify(player2, never()).payFood(anyInt(), anyInt());
        verify(player3, never()).payFood(anyInt(), anyInt());
    }

    // -------------------------------------------------------------------------
    // returnTotem – zero reward edge case
    // -------------------------------------------------------------------------

    @Test
    void shouldNotGrantExtraFoodRightWhenPositionOneRewardIsZero() {
        OfferTurnCard zeroRewardCard = new OfferTurnCard(NUM_PLAYERS, List.of(0, 0, 0, 0), PP_PENALTY);
        zeroRewardCard.returnTotem(player1);
        verify(player1).setExtraFoodRight(false);
        verify(player1, never()).setExtraFoodRight(true);
    }

    @Test
    void shouldNotGrantExtraFoodRightWhenPositionTwoRewardIsZero() {
        OfferTurnCard zeroRewardCard = new OfferTurnCard(NUM_PLAYERS, List.of(0, 0, 0, 0), PP_PENALTY);
        zeroRewardCard.returnTotem(player1);
        zeroRewardCard.returnTotem(player2);
        verify(player2).setExtraFoodRight(false);
        verify(player2, never()).setExtraFoodRight(true);
    }

    // -------------------------------------------------------------------------
    // toSnapshot
    // -------------------------------------------------------------------------

    /*
    @Test
    void shouldReturnNonNullSnapshot() {
        // TODO: replace the stubbed enum type with the actual Totem enum once confirmed
        // e.g.: when(player1.getTotem()).thenReturn(Totem.RED);
        offerTurnCard.returnTotem(player1);
        offerTurnCard.returnTotem(player2);

        // Mock the totem enum using the actual enum class
        var totem1 = mock(player1.getClass()); // placeholder – see TODO above
        // when(player1.getTotem()).thenReturn(Totem.RED);
        // when(player2.getTotem()).thenReturn(Totem.BLUE);

        OfferTurnCardSnapshot snapshot = offerTurnCard.toSnapshot();
        assertNotNull(snapshot);
    } */

    @Test
    void shouldReturnSnapshotWithTotemNamesInCorrectOrder() {
        // TODO: replace with actual Totem enum values, e.g.:
        // when(player1.getTotem()).thenReturn(Totem.RED);
        // when(player2.getTotem()).thenReturn(Totem.BLUE);
        // OfferTurnCardSnapshot snapshot = offerTurnCard.toSnapshot();
        // assertEquals(List.of("RED", "BLUE"), snapshot.totemOrder());
    }
}