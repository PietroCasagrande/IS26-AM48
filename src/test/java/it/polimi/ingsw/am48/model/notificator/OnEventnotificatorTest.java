package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnEventNotificatorTest {

    private OnEventNotificator notificator;

    @Mock private Player player1;
    @Mock private Player player2;
    @Mock private PlayerContext playerContext;
    @Mock private CardStrategy strategy1;
    @Mock private CardStrategy strategy2;
    @Mock private CardStrategy strategy3;

    @BeforeEach
    void setUp() {
        notificator = new OnEventNotificator();
    }

    // ── ATTACH ────────────────────────────────────────────────────────────────

    @Test
    void attach_createsNestedStructureCorrectly() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        // verifichiamo indirettamente tramite notify
        when(playerContext.getCurrPlayer()).thenReturn(player1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(strategy1).effect(playerContext);
    }

    @Test
    void attach_samePlayerSameEvent_addsBothStrategies() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy2);
        when(playerContext.getCurrPlayer()).thenReturn(player1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(strategy1).effect(playerContext);
        verify(strategy2).effect(playerContext);
    }

    @Test
    void attach_differentEvents_areIndependent() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.ARTIST_EVENT, player1, strategy2);
        when(playerContext.getCurrPlayer()).thenReturn(player1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(strategy1).effect(playerContext);
        verify(strategy2, never()).effect(playerContext);
    }

    // ── DETACH ────────────────────────────────────────────────────────────────


    @Test
    void detach_nonExistentEventType_throwsStrategyNotFoundException() {
        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, strategy1));
    }

    @Test
    void detach_nonExistentPlayer_throwsStrategyNotFoundException() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player2, strategy1));
    }

    @Test
    void detach_nonExistentStrategy_throwsStrategyNotFoundException() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);

        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(EventType.HUNTER_EVENT, player1, strategy2));
    }

    // ── NOTIFY ────────────────────────────────────────────────────────────────

    @Test
    void notify_noListenersForEvent_doesNothing() {
        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verifyNoInteractions(playerContext);
        verifyNoInteractions(strategy1);
    }

    @Test
    void notify_eventWithNoRegisteredPlayers_doesNothing() {
        notificator.attach(EventType.ARTIST_EVENT, player1, strategy1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verifyNoInteractions(playerContext);
        verifyNoInteractions(strategy1);
    }

    @Test
    void notify_multiplePlayersSameEvent_callsAllEffects() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.HUNTER_EVENT, player2, strategy2);
        when(playerContext.getCurrPlayer()).thenReturn(player1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(strategy1).effect(playerContext);
        verify(strategy2).effect(playerContext);
    }

    @Test
    void notify_restoresPreviousCurrPlayer() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        when(playerContext.getCurrPlayer()).thenReturn(player2); // player2 was the current

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(playerContext).setCurrPlayer(player2); // reset in the end
    }

    @Test
    void notify_doesNotAffectOtherEventTypes() {
        notificator.attach(EventType.HUNTER_EVENT, player1, strategy1);
        notificator.attach(EventType.ARTIST_EVENT, player1, strategy2);
        when(playerContext.getCurrPlayer()).thenReturn(player1);

        notificator.notify(EventType.HUNTER_EVENT, playerContext);

        verify(strategy1).effect(playerContext);
        verify(strategy2, never()).effect(playerContext);
    }
}