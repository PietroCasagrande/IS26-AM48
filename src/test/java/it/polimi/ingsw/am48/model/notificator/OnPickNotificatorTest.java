package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link OnPickNotificator}.
 * Verifies that strategies registered per player fire on the correct context,
 * that multiple strategies for the same player all execute, that listeners
 * are isolated between players, and that the notificator is persistent
 * across multiple notify calls.
 */
class OnPickNotificatorTest {

    private OnPickNotificator notificator;
    private Player player1;
    private Player player2;
    private PlayerContext context1;
    private PlayerContext context2;
    private CardStrategy strategy1;
    private CardStrategy strategy2;

    @BeforeEach
    void setUp() {
        notificator = new OnPickNotificator();

        player1  = mock(Player.class);
        player2  = mock(Player.class);
        context1 = mock(PlayerContext.class);
        context2 = mock(PlayerContext.class);

        when(context1.getCurrPlayer()).thenReturn(player1);
        when(context2.getCurrPlayer()).thenReturn(player2);

        strategy1 = mock(CardStrategy.class);
        strategy2 = mock(CardStrategy.class);
    }

    // ==================== attach ====================

    /**
     * Tests that a strategy registered via
     * {@link OnPickNotificator#attach(Player, CardStrategy)} fires when
     * {@link OnPickNotificator#notify(PlayerContext)} is called with the
     * correct player context.
     * <p>Components involved: {@link OnPickNotificator}, {@link Player},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach: should fire strategy on notify after registration")
    void shouldFireStrategyOnNotifyAfterRegistration() {
        notificator.attach(player1, strategy1);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
    }

    /**
     * Tests that multiple strategies registered for the same player all
     * fire when the player's context is notified.
     * <p>Components involved: {@link OnPickNotificator}, {@link Player},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach: should fire all strategies when multiple are registered for the same player")
    void shouldFireAllStrategiesForSamePlayer() {
        notificator.attach(player1, strategy1);
        notificator.attach(player1, strategy2);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
        verify(strategy2, times(1)).effect(any());
    }

    /**
     * Tests that strategies registered for different players are isolated:
     * notifying one player's context does not trigger another player's
     * strategies.
     * <p>Components involved: {@link OnPickNotificator}, {@link Player},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach: should keep listeners separate for different players")
    void shouldKeepListenersSeparateForDifferentPlayers() {
        notificator.attach(player1, strategy1);
        notificator.attach(player2, strategy2);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
        verify(strategy2, never()).effect(any());
    }

    // ==================== notify ====================

    /**
     * Tests that notifying with no strategies registered for the current
     * player does not throw.
     * <p>Components involved: {@link OnPickNotificator},
     * {@link PlayerContext}.</p>
     */
    @Test
    @DisplayName("notify: should not throw when no strategies are registered for the player")
    void shouldNotThrowWhenNoStrategiesRegistered() {
        assertDoesNotThrow(() -> notificator.notify(context1));
    }

    /**
     * Tests that registered strategies persist across multiple notify calls
     * and fire each time.
     * <p>Components involved: {@link OnPickNotificator}, {@link Player},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("notify: should fire strategy persistently on repeated notify calls")
    void shouldFireStrategyPersistentlyOnRepeatedNotify() {
        notificator.attach(player1, strategy1);

        notificator.notify(context1);
        notificator.notify(context1);

        verify(strategy1, times(2)).effect(any());
    }

    /**
     * Tests that notifying one player does not cross-contaminate another
     * player's strategies.
     * <p>Components involved: {@link OnPickNotificator}, {@link Player},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("notify: should not affect other players' strategies")
    void shouldNotCrossContaminateBetweenPlayers() {
        notificator.attach(player1, strategy1);
        notificator.attach(player2, strategy2);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
        verify(strategy2, never()).effect(any());
    }
}