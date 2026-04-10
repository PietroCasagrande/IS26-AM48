package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnEventNotificatorTest {

    private OnEventNotificator notificator;
    private Player player1;
    private Player player2;
    private PlayerContext context;
    private CardStrategy strategy1;
    private CardStrategy strategy2;

    @BeforeEach
    void setUp() {
        notificator = new OnEventNotificator();

        player1  = mock(Player.class);
        player2  = mock(Player.class);
        context  = mock(PlayerContext.class);

        when(context.getCurrPlayer()).thenReturn(player1);

        strategy1 = mock(CardStrategy.class);
        strategy2 = mock(CardStrategy.class);
    }

    // ==================== attach ====================

    @Test
    @DisplayName("attach: should fire strategy on notify after registration")
    void shouldFireStrategyOnNotifyAfterRegistration() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, times(1)).effect(context);
    }

    @Test
    @DisplayName("attach: should fire all strategies when multiple are registered for same player and event")
    void shouldFireAllStrategiesForSamePlayerAndEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, times(1)).effect(context);
        verify(strategy2, times(1)).effect(context);
    }

    @Test
    @DisplayName("attach: should fire strategies for both players when registered on same event")
    void shouldFireStrategiesForBothPlayersOnSameEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player2, strategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, times(1)).effect(context);
        verify(strategy2, times(1)).effect(context);
    }

    @Test
    @DisplayName("attach: should not fire strategy registered for a different event")
    void shouldNotFireStrategyRegisteredForDifferentEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.SHAMAN_EVENT, player1, strategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, times(1)).effect(context);
        verify(strategy2, never()).effect(any());
    }

    // ==================== detach ====================

    @Test
    @DisplayName("detach: should not fire strategy after it has been detached")
    void shouldNotFireStrategyAfterDetach() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.detach(EventType.HUNTER_EVENT, player1, strategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("detach: should only remove the target strategy leaving others intact")
    void shouldOnlyRemoveTargetStrategyLeavingOthersIntact() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy2);

        notificator.detach(EventType.HUNTER_EVENT, player1, strategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, never()).effect(any());
        verify(strategy2, times(1)).effect(context);
    }

    @Test
    @DisplayName("detach: should not throw when notifying after last strategy for a player is removed")
    void shouldNotThrowWhenNotifyingAfterLastStrategyRemoved() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.detach(EventType.HUNTER_EVENT, player1, strategy1);

        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("detach: should not throw when notifying after last player for an event is removed")
    void shouldNotThrowWhenNotifyingAfterLastPlayerForEventRemoved() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.detach(EventType.HUNTER_EVENT, player1, strategy1);

        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
    }

    @Test
    @DisplayName("detach: should throw StrategyNotFoundException when event is not registered")
    void shouldThrowWhenEventNotRegistered() {
        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, strategy1));
    }

    @Test
    @DisplayName("detach: should throw StrategyNotFoundException when player is not registered for event")
    void shouldThrowWhenPlayerNotRegisteredForEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player2, strategy1));
    }

    @Test
    @DisplayName("detach: should throw StrategyNotFoundException when strategy is not registered for player")
    void shouldThrowWhenStrategyNotRegisteredForPlayer() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, strategy2));
    }

    // ==================== notify ====================

    @Test
    @DisplayName("notify: should not throw when no strategies are registered for the event")
    void shouldNotThrowWhenNoStrategiesRegisteredForEvent() {
        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
    }

    @Test
    @DisplayName("notify: should fire strategy multiple times on repeated notify calls")
    void shouldFireStrategyMultipleTimesOnRepeatedNotify() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(strategy1, times(2)).effect(context);
    }

    @Test
    @DisplayName("notify: should not fire strategies registered for a different event")
    void shouldNotFireStrategiesForWrongEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        notificator.notify(EventType.SHAMAN_EVENT, context);

        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("notify: should call setCurrPlayer for each registered player")
    void shouldSetCurrPlayerForEachRegisteredPlayer() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player2, strategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(context, atLeastOnce()).setCurrPlayer(player1);
        verify(context, atLeastOnce()).setCurrPlayer(player2);
    }

    @Test
    @DisplayName("notify: should restore the original currPlayer after notify completes")
    void shouldRestoreCurrPlayerAfterNotify() {
        Player previousPlayer = mock(Player.class);
        when(context.getCurrPlayer()).thenReturn(previousPlayer);

        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(context).setCurrPlayer(previousPlayer);
    }
}