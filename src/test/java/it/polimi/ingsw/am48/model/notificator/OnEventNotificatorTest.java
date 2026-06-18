package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link OnEventNotificator}.
 * Verifies the two-listener architecture: persistent building strategies
 * and one-shot event strategies. Also verifies event type scoping,
 * context switching for building listeners, and proper restoration of
 * the current player.
 */
class OnEventNotificatorTest {

    private OnEventNotificator notificator;
    private Player player1;
    private Player player2;
    private PlayerContext context;
    private CardStrategy buildingStrategy1;
    private CardStrategy buildingStrategy2;
    private CardStrategy eventStrategy1;
    private CardStrategy eventStrategy2;

    @BeforeEach
    void setUp() {
        notificator = new OnEventNotificator();

        player1 = mock(Player.class);
        player2 = mock(Player.class);
        context = mock(PlayerContext.class);

        when(context.getCurrPlayer()).thenReturn(player1);

        buildingStrategy1 = mock(CardStrategy.class);
        buildingStrategy2 = mock(CardStrategy.class);
        eventStrategy1    = mock(CardStrategy.class);
        eventStrategy2    = mock(CardStrategy.class);
    }

    // ==================== attach (building - persistent) ====================

    /**
     * Tests that a building strategy does not fire when no event strategy
     * is registered for the same event type (notify returns early).
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should not fire building strategy if no event strategy is registered")
    void shouldNotFireBuildingStrategyIfNoEventStrategyRegistered() {
        // notify returns immediately if the event is not in eventListeners
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(buildingStrategy1, never()).effect(any());
    }

    /**
     * Tests that a building strategy fires when an event strategy is also
     * registered for the same event type.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should fire building strategy when event strategy is also registered")
    void shouldFireBuildingStrategyWhenEventStrategyIsAlsoRegistered() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(buildingStrategy1, times(1)).effect(context);
    }

    /**
     * Tests that multiple building strategies for the same player and event
     * type all fire on notification.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should fire all building strategies for same player and event")
    void shouldFireAllBuildingStrategiesForSamePlayerAndEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy2);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(buildingStrategy1, times(1)).effect(context);
        verify(buildingStrategy2, times(1)).effect(context);
    }

    /**
     * Tests that building strategies for different players are all executed
     * when the corresponding event fires.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should fire building strategies for both players on same event")
    void shouldFireBuildingStrategiesForBothPlayersOnSameEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, player2, buildingStrategy2);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(buildingStrategy1, times(1)).effect(context);
        verify(buildingStrategy2, times(1)).effect(context);
    }

    /**
     * Tests that a building strategy registered for one event type does not
     * fire when a different event type is notified.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should not fire building strategy registered for a different event")
    void shouldNotFireBuildingStrategyForDifferentEvent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.SHAMAN_EVENT, eventStrategy1);

        notificator.notify(EventType.SHAMAN_EVENT, context);

        verify(buildingStrategy1, never()).effect(any());
    }

    /**
     * Tests that building strategies are persistent: they fire on every
     * notification call, not just once.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(building): should fire building strategy persistently across multiple notify calls")
    void shouldFireBuildingStrategyPersistentlyAcrossMultipleNotifyCalls() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        // re-register event strategies since eventListeners is cleared after notify
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);
        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(buildingStrategy1, times(2)).effect(context);
    }

    // ==================== attach (event - one-shot) ====================

    /**
     * Tests that a one-shot event strategy fires when notified.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(event): should fire event strategy on notify")
    void shouldFireEventStrategyOnNotify() {
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(eventStrategy1, times(1)).effect(context);
    }

    /**
     * Tests that all event strategies registered for the same event type
     * fire on notification.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(event): should fire all event strategies registered for same event")
    void shouldFireAllEventStrategiesForSameEvent() {
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(eventStrategy1, times(1)).effect(context);
        verify(eventStrategy2, times(1)).effect(context);
    }

    /**
     * Tests that an event strategy registered for one event type does not
     * fire when a different event type is notified.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(event): should not fire event strategy registered for a different event")
    void shouldNotFireEventStrategyForDifferentEvent() {
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);
        notificator.attach(EventType.SHAMAN_EVENT, eventStrategy2);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(eventStrategy1, times(1)).effect(context);
        verify(eventStrategy2, never()).effect(any());
    }

    /**
     * Tests that event strategies are one-shot: they are cleared after
     * notification and do not fire on subsequent notify calls.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("attach(event): should remove all event strategies after notify (one-shot behaviour)")
    void shouldRemoveAllEventStrategiesAfterNotify() {
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);
        // re-attach event to trigger a second notify
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy2);
        notificator.notify(EventType.HUNTER_EVENT, context);

        // eventStrategy1 fired only once (cleared after first notify)
        verify(eventStrategy1, times(1)).effect(context);
        // eventStrategy2 fired only in the second notify
        verify(eventStrategy2, times(1)).effect(context);
    }

    // ==================== notify ====================

    /**
     * Tests that {@link OnEventNotificator#notify(EventType, PlayerContext)}
     * does nothing when no event strategy is registered for the given type,
     * even if building strategies exist.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("notify: should do nothing when event is not registered in eventListeners")
    void shouldDoNothingWhenEventNotInEventListeners() {
        // building strategy registered, but no event strategy → early return
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);

        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
        verify(buildingStrategy1, never()).effect(any());
    }

    /**
     * Tests that notifying with no registrations at all does not throw.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link PlayerContext}.</p>
     */
    @Test
    @DisplayName("notify: should not throw when nothing is registered at all")
    void shouldNotThrowWhenNothingRegistered() {
        assertDoesNotThrow(() -> notificator.notify(EventType.HUNTER_EVENT, context));
    }

    /**
     * Tests that the current player is switched to each building listener's
     * owner before their strategy executes.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("notify: should call setCurrPlayer for each building listener player")
    void shouldSetCurrPlayerForEachBuildingListenerPlayer() {
        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, player2, buildingStrategy2);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(context, atLeastOnce()).setCurrPlayer(player1);
        verify(context, atLeastOnce()).setCurrPlayer(player2);
    }

    /**
     * Tests that after all building listeners have been notified, the current
     * player is restored to the one that was active before the notification.
     * <p>Components involved: {@link OnEventNotificator}, {@link EventType},
     * {@link Player}, {@link PlayerContext}, {@link CardStrategy}.</p>
     */
    @Test
    @DisplayName("notify: should restore the original currPlayer after notify completes")
    void shouldRestoreCurrPlayerAfterNotify() {
        Player previousPlayer = mock(Player.class);
        when(context.getCurrPlayer()).thenReturn(previousPlayer);

        notificator.attach(EventType.HUNTER_EVENT, player1, buildingStrategy1);
        notificator.attach(EventType.HUNTER_EVENT, eventStrategy1);

        notificator.notify(EventType.HUNTER_EVENT, context);

        verify(context).setCurrPlayer(previousPlayer);
    }
}