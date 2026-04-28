package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("attach: should fire strategy on notify after registration")
    void shouldFireStrategyOnNotifyAfterRegistration() {
        notificator.attach(player1, strategy1);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
    }

    @Test
    @DisplayName("attach: should fire all strategies when multiple are registered for the same player")
    void shouldFireAllStrategiesForSamePlayer() {
        notificator.attach(player1, strategy1);
        notificator.attach(player1, strategy2);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(any());
        verify(strategy2, times(1)).effect(any());
    }

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

    @Test
    @DisplayName("notify: should not throw when no strategies are registered for the player")
    void shouldNotThrowWhenNoStrategiesRegistered() {
        assertDoesNotThrow(() -> notificator.notify(context1));
    }

    @Test
    @DisplayName("notify: should fire strategy persistently on repeated notify calls")
    void shouldFireStrategyPersistentlyOnRepeatedNotify() {
        notificator.attach(player1, strategy1);

        notificator.notify(context1);
        notificator.notify(context1);

        verify(strategy1, times(2)).effect(any());
    }

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