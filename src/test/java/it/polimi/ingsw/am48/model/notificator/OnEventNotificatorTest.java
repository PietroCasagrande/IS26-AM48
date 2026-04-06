package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnEventNotificatorTest {

    private OnEventNotificator notificator;

    private Player player1;
    private Player player2;
    private PlayerContext context;
    private CardStrategy persistent;
    private CardStrategy oneShot;

    @BeforeEach
    void setUp() {
        notificator = new OnEventNotificator();

        player1  = mock(Player.class);
        player2  = mock(Player.class);
        context  = mock(PlayerContext.class);

        when(context.getCurrPlayer()).thenReturn(player1);

        // persistent strategy: unregisterFrom does not add itself to toDetach
        persistent = mock(CardStrategy.class);
        doNothing().when(persistent).effect(any());
        doNothing().when(persistent).unregisterFrom(anyList());

        // one-shot strategy: unregisterFrom adds itself to toDetach
        oneShot = mock(CardStrategy.class);
        doNothing().when(oneShot).effect(any());
        doAnswer(inv -> {
            List<CardStrategy> list = inv.getArgument(0);
            list.add(oneShot);
            return null;
        }).when(oneShot).unregisterFrom(anyList());
    }

    // ATTACH

    @Test
    void attach_singleStrategy_firedOnNotify() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(1)).effect(context);
    }

    @Test
    void attach_multipleStrategiesSamePlayerAndEvent_allFired() {
        CardStrategy second = mock(CardStrategy.class);
        doNothing().when(second).unregisterFrom(anyList());

        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.HUNTER_EVENT, player1, second);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(1)).effect(context);
        verify(second, times(1)).effect(context);
    }

    @Test
    void attach_differentPlayersSameEvent_bothFired() {
        // context switches currPlayer per entry in notify
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.HUNTER_EVENT, player2, oneShot);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(1)).effect(context);
        verify(oneShot, times(1)).effect(context);
    }

    @Test
    void attach_differentEvents_noInterference() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.SHAMAN_EVENT, player1, oneShot);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(1)).effect(context);
        verify(oneShot, never()).effect(any());
    }

    // DETACH

    @Test
    void detach_existingStrategy_removedSuccessfully() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.detach(EventType.HUNTER_EVENT, player1, persistent);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, never()).effect(any());
    }

    @Test
    void detach_oneOfMultiple_onlyTargetRemoved() {
        CardStrategy other = mock(CardStrategy.class);
        doNothing().when(other).unregisterFrom(anyList());

        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.HUNTER_EVENT, player1, other);

        notificator.detach(EventType.HUNTER_EVENT, player1, persistent);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, never()).effect(any());
        verify(other, times(1)).effect(context);
    }

    @Test
    void detach_lastStrategyForPlayer_playerEntryRemoved() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.detach(EventType.HUNTER_EVENT, player1, persistent);

        // notify must not throw and must not fire anything
        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
        verify(persistent, never()).effect(any());
    }

    @Test
    void detach_lastPlayerForEvent_eventEntryRemoved() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.detach(EventType.HUNTER_EVENT, player1, persistent);

        // notify on now-empty event must not throw
        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
    }

    @Test
    void detach_eventNotRegistered_throwsException() {
        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, persistent));
    }

    @Test
    void detach_playerNotRegisteredForEvent_throwsException() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player2, persistent));
    }

    @Test
    void detach_strategyNotRegisteredForPlayer_throwsException() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        CardStrategy unregistered = mock(CardStrategy.class);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, unregistered));
    }

    // NOTIFY

    @Test
    void notify_noStrategiesForEvent_doesNothing() {
        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
    }

    @Test
    void notify_oneShotStrategy_removedAfterFirstFire() {
        notificator.attach(EventType.HUNTER_EVENT, player1, oneShot);

        notificator.notify(EventType.HUNTER_EVENT, context);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(oneShot, times(1)).effect(context);
    }

    @Test
    void notify_persistentStrategy_remainsAfterFire() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);

        notificator.notify(EventType.HUNTER_EVENT, context);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(2)).effect(context);
    }

    @Test
    void notify_mixedStrategies_onlyOneShotRemoved() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.HUNTER_EVENT, player1, oneShot);

        notificator.notify(EventType.HUNTER_EVENT, context);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(persistent, times(2)).effect(context);
        verify(oneShot, times(1)).effect(context);
    }

    @Test
    void notify_setCurrPlayerCalledPerPlayer() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.attach(EventType.HUNTER_EVENT, player2, oneShot);

        notificator.notify(EventType.HUNTER_EVENT, context);

        // context.setCurrPlayer must have been called for player1 and player2
        verify(context, atLeastOnce()).setCurrPlayer(player1);
        verify(context, atLeastOnce()).setCurrPlayer(player2);
    }

    @Test
    void notify_currPlayerRestoredAfterNotify() {
        Player previousPlayer = mock(Player.class);
        when(context.getCurrPlayer()).thenReturn(previousPlayer);

        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);
        notificator.notify(EventType.HUNTER_EVENT, context);

        // the last setCurrPlayer call must restore the original player
        verify(context).setCurrPlayer(previousPlayer);
    }

    @Test
    void notify_wrongEvent_doesNotFireStrategies() {
        notificator.attach(EventType.HUNTER_EVENT, player1, persistent);

        notificator.notify(EventType.SHAMAN_EVENT, context);

        verify(persistent, never()).effect(any());
    }
}