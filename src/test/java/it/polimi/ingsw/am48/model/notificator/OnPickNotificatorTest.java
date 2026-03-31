package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.exception.StrategyNotFoundException;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnPickNotificatorTest {

    private OnPickNotificator notificator;

    private Player player1;
    private Player player2;
    private PlayerContext context1;
    private PlayerContext context2;

    private CardStrategy persistent;   // never adding to detach
    private CardStrategy oneShot;      // adding to detach once

    @BeforeEach
    void setUp() {
        notificator = new OnPickNotificator();

        player1  = mock(Player.class);
        player2  = mock(Player.class);
        context1 = mock(PlayerContext.class);
        context2 = mock(PlayerContext.class);

        when(context1.getCurrPlayer()).thenReturn(player1);
        when(context2.getCurrPlayer()).thenReturn(player2);

        // persistent strategy, unregisterfrom does not add anything
        persistent = mock(CardStrategy.class);
        doNothing().when(persistent).effect(any());
        doNothing().when(persistent).unregisterFrom(anyList());

        // one-shot strategy, unregisterfrom adds itself to detach
        oneShot = mock(CardStrategy.class);
        doNothing().when(oneShot).effect(any());
        doAnswer(invocation -> {
            List<CardStrategy> list = invocation.getArgument(0);
            list.add(oneShot);
            return null;
        }).when(oneShot).unregisterFrom(anyList());
    }


    // ATTACH


    @Test
    @DisplayName("attach: registra una strategia per un giocatore")
    void attach_singleStrategy_registersCorrectly() {
        notificator.attach(player1, persistent);
        // verifica indirettamente tramite notify: effect deve essere chiamato
        notificator.notify(context1);
        verify(persistent, times(1)).effect(any());
    }

    @Test
    @DisplayName("attach: registra più strategie per lo stesso giocatore")
    void attach_multipleStrategies_allRegistered() {
        CardStrategy second = mock(CardStrategy.class);
        doNothing().when(second).unregisterFrom(anyList());

        notificator.attach(player1, persistent);
        notificator.attach(player1, second);

        notificator.notify(context1);

        verify(persistent, times(1)).effect(any());
        verify(second, times(1)).effect(any());
    }

    @Test
    @DisplayName("attach: giocatori diversi hanno listener separati")
    void attach_differentPlayers_independentListeners() {
        notificator.attach(player1, persistent);
        notificator.attach(player2, oneShot);

        notificator.notify(context1);

        // just player 1 is considered, not player 2
        verify(persistent, times(1)).effect(any());
        verify(oneShot, never()).effect(any());
    }


    // DETACH


    @Test
    @DisplayName("detach: rimuove correttamente la strategia specificata")
    void detach_existingStrategy_removedSuccessfully() {
        notificator.attach(player1, persistent);
        notificator.detach(player1, persistent);

        // notify does not call effect after something has been removed
        notificator.notify(context1);
        verify(persistent, never()).effect(any());
    }

    @Test
    @DisplayName("detach: rimuove solo la strategia specificata, non le altre")
    void detach_oneOfMultiple_onlyTargetRemoved() {
        CardStrategy other = mock(CardStrategy.class);
        doNothing().when(other).unregisterFrom(anyList());

        notificator.attach(player1, persistent);
        notificator.attach(player1, other);

        notificator.detach(player1, persistent);

        notificator.notify(context1);

        verify(persistent, never()).effect(any());
        verify(other, times(1)).effect(any());
    }

    @Test
    @DisplayName("detach: rimuovendo l'ultima strategia, il player viene rimosso dalla mappa")
    void detach_lastStrategy_playerRemovedFromMap() {
        notificator.attach(player1, persistent);
        notificator.detach(player1, persistent);

        // notify with an empty map does not cause an exception
        assertDoesNotThrow(() -> notificator.notify(context1));
        verify(persistent, never()).effect(any());
    }

    @Test
    @DisplayName("detach: strategia non presente lancia StrategyNotFoundException")
    void detach_strategyNotFound_throwsException() {
        // player not registred
        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(player1, persistent));
    }

    @Test
    @DisplayName("detach: strategia non registrata per quel player lancia StrategyNotFoundException")
    void detach_strategyNotRegisteredForPlayer_throwsException() {
        notificator.attach(player1, persistent);
        CardStrategy unregistered = mock(CardStrategy.class);

        // persistent is registered, not unregister from
        assertThrows(StrategyNotFoundException.class,
                () -> notificator.detach(player1, unregistered));
    }


    // NOTIFY


    @Test
    @DisplayName("notify: player senza strategie registrate non lancia eccezioni")
    void notify_noStrategiesRegistered_doesNothing() {
        assertDoesNotThrow(() -> notificator.notify(context1));
    }

    @Test
    @DisplayName("notify: chiama effect su tutte le strategie del player")
    void notify_callsEffectOnAllStrategies() {
        CardStrategy s1 = mock(CardStrategy.class);
        CardStrategy s2 = mock(CardStrategy.class);
        doNothing().when(s1).unregisterFrom(anyList());
        doNothing().when(s2).unregisterFrom(anyList());

        notificator.attach(player1, s1);
        notificator.attach(player1, s2);

        notificator.notify(context1);

        verify(s1, times(1)).effect(any());
        verify(s2, times(1)).effect(any());
    }

    @Test
    @DisplayName("notify: strategia one-shot viene rimossa dopo il primo fire")
    void notify_oneShotStrategy_removedAfterFire() {
        notificator.attach(player1, oneShot);

        notificator.notify(context1);
        notificator.notify(context1);

        verify(oneShot, times(1)).effect(any());
    }

    @Test
    @DisplayName("notify: strategia persistente rimane dopo il fire")
    void notify_persistentStrategy_remainsAfterFire() {
        notificator.attach(player1, persistent);

        notificator.notify(context1);
        notificator.notify(context1);

        verify(persistent, times(2)).effect(any());
    }

    @Test
    @DisplayName("notify: mix one-shot e persistente — solo one-shot rimossa")
    void notify_mixedStrategies_onlyOneShotRemoved() {
        notificator.attach(player1, persistent);
        notificator.attach(player1, oneShot);

        notificator.notify(context1);
        notificator.notify(context1);

        verify(persistent, times(2)).effect(any());
        verify(oneShot, times(1)).effect(any());
    }

    @Test
    @DisplayName("notify: rimozione di tutte le one-shot svuota la mappa per quel player")
    void notify_allOneShot_playerRemovedFromMapAfterFire() {
        notificator.attach(player1, oneShot);

        notificator.notify(context1);

        assertDoesNotThrow(() -> notificator.notify(context1));
        verify(oneShot, times(1)).effect(any());
    }

    @Test
    @DisplayName("notify: player2 non viene influenzato dalla notify di player1")
    void notify_doesNotCrossContaminate() {
        notificator.attach(player1, persistent);
        notificator.attach(player2, oneShot);

        notificator.notify(context1);

        verify(persistent, times(1)).effect(any());
        verify(oneShot, never()).effect(any());
    }
}