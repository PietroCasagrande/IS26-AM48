package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnTotemReturnedNotificatorTest {

    private OnTotemReturnedNotificator notificator;

    private Player player1;
    private Player player2;
    private PlayerContext context1;
    private PlayerContext context2;
    private CardStrategy strategy1;
    private CardStrategy strategy2;

    @BeforeEach
    void setUp() {
        notificator = new OnTotemReturnedNotificator();

        player1 = mock(Player.class);
        player2 = mock(Player.class);
        context1 = mock(PlayerContext.class);
        context2 = mock(PlayerContext.class);
        strategy1 = mock(CardStrategy.class);
        strategy2 = mock(CardStrategy.class);

        when(context1.getCurrPlayer()).thenReturn(player1);
        when(context2.getCurrPlayer()).thenReturn(player2);
    }

    // ATTACH

    @Test
    @DisplayName("attach: strategy is correctly registered and activated on notify")
    void attach_singleStrategy_activatedOnNotify() {
        notificator.attach(player1, strategy1);
        notificator.notify(context1);

        verify(strategy1, times(1)).effect(context1);
    }

    @Test
    @DisplayName("attach: second strategy for same player overwrites the first")
    void attach_secondStrategy_overwritesFirst() {
        notificator.attach(player1, strategy1);
        notificator.attach(player1, strategy2);

        notificator.notify(context1);

        // only the last registered strategy should fire
        verify(strategy2, times(1)).effect(context1);
        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("attach: different players have independent strategies")
    void attach_differentPlayers_independentStrategies() {
        notificator.attach(player1, strategy1);
        notificator.attach(player2, strategy2);

        notificator.notify(context1);

        verify(strategy1, times(1)).effect(context1);
        verify(strategy2, never()).effect(any());
    }

    // NOTIFY

    @Test
    @DisplayName("notify: player with no registered strategy does nothing")
    void notify_noStrategyRegistered_doesNothing() {
        assertDoesNotThrow(() -> notificator.notify(context1));
        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("notify: strategy is persistent and fires every time")
    void notify_persistentStrategy_firesEveryTime() {
        notificator.attach(player1, strategy1);

        notificator.notify(context1);
        notificator.notify(context1);
        notificator.notify(context1);

        // the building effect must trigger every time the totem is returned
        verify(strategy1, times(3)).effect(context1);
    }

    @Test
    @DisplayName("notify: notifying player2 does not affect player1's strategy")
    void notify_differentPlayer_doesNotCrossContaminate() {
        notificator.attach(player1, strategy1);

        // notifying player2 who has no strategy
        notificator.notify(context2);

        verify(strategy1, never()).effect(any());
    }

    @Test
    @DisplayName("notify: uses getCurrPlayer() to resolve the correct player")
    void notify_usesCurrPlayer_toResolveCorrectPlayer() {
        notificator.attach(player1, strategy1);
        notificator.notify(context1);

        // verifies that getCurrPlayer() was called to retrieve the key
        verify(context1, atLeastOnce()).getCurrPlayer();
    }
}