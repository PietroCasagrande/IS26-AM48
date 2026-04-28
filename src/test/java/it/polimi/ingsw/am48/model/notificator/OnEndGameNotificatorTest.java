package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnEndGameNotificatorTest {

    private OnEndGameNotificator notificator;

    @Mock private Player player1;
    @Mock private Player player2;
    @Mock private PlayerContext playerContext;
    @Mock private CardStrategy strategy1;
    @Mock private CardStrategy strategy2;
    @Mock private CardStrategy strategy3;

    @BeforeEach
    void setUp() {
        notificator = new OnEndGameNotificator();
    }

    @Test
    void notify_noListeners_doesNothing() {
        notificator.notify(playerContext);

        verifyNoInteractions(playerContext);
        verifyNoInteractions(strategy1);
    }

    @Test
    void notify_playerWithNoStrategies_isSkipped() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1));
        // player1 not registered in the map

        notificator.attach(player2, strategy1); // registered only player 2
        notificator.notify(playerContext);

        // player1 has no strategies
        verify(playerContext, never()).setCurrPlayer(player1);
        verifyNoInteractions(strategy1);
    }

    @Test
    void notify_singlePlayerSingleStrategy_callsEffectCorrectly() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1));

        notificator.attach(player1, strategy1);
        notificator.notify(playerContext);

        verify(playerContext).setCurrPlayer(player1);
        verify(strategy1).effect(playerContext);
    }

    @Test
    void notify_singlePlayerMultipleStrategies_callsAllEffects() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1));

        notificator.attach(player1, strategy1);
        notificator.attach(player1, strategy2);
        notificator.notify(playerContext);

        verify(playerContext).setCurrPlayer(player1);
        verify(strategy1).effect(playerContext);
        verify(strategy2).effect(playerContext);
    }

    @Test
    void notify_multiplePlayers_setsEachCurrPlayerAndCallsEffects() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1, player2));

        notificator.attach(player1, strategy1);
        notificator.attach(player2, strategy2);
        notificator.notify(playerContext);

        verify(playerContext).setCurrPlayer(player1);
        verify(strategy1).effect(playerContext);
        verify(playerContext).setCurrPlayer(player2);
        verify(strategy2).effect(playerContext);
    }

    @Test
    void notify_multiplePlayersOneHasNoStrategies_onlyNotifiesRegistered() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1, player2));

        notificator.attach(player1, strategy1);
        // player2 not registered
        notificator.notify(playerContext);

        verify(playerContext).setCurrPlayer(player1);
        verify(strategy1).effect(playerContext);
        verify(playerContext, never()).setCurrPlayer(player2);
        verifyNoInteractions(strategy2);
    }

    @Test
    void notify_respectsPlayerOrder() {
        when(playerContext.getPlayers()).thenReturn(List.of(player1, player2));

        notificator.attach(player1, strategy1);
        notificator.attach(player2, strategy2);
        notificator.notify(playerContext);

        var inOrder = inOrder(playerContext, strategy1, strategy2);
        inOrder.verify(playerContext).setCurrPlayer(player1);
        inOrder.verify(strategy1).effect(playerContext);
        inOrder.verify(playerContext).setCurrPlayer(player2);
        inOrder.verify(strategy2).effect(playerContext);
    }
}