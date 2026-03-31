package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnEndOfferPhaseNotificatorTest {

    private OnEndOfferPhaseNotificator notificator;

    @Mock private Player cardOwner;
    @Mock private Player previousPlayer;
    @Mock private PlayerContext playerContext;
    @Mock private CardStrategy strategy;

    @BeforeEach
    void setUp() {
        notificator = new OnEndOfferPhaseNotificator();
    }

    @Test
    void notify_noListeners_doesNothing() {
        notificator.notify(playerContext);

        verifyNoInteractions(playerContext, strategy);
    }

    @Test
    void notify_withListener_setsOwnerCallsEffectAndRestoresPrevious() {
        when(playerContext.getCurrPlayer()).thenReturn(previousPlayer);

        notificator.attach(cardOwner, strategy);
        notificator.notify(playerContext);

        // it has to set up the card owner
        verify(playerContext).setCurrPlayer(cardOwner);
        // it has to call the effect
        verify(strategy).effect(playerContext);
        // it has to reset the previous player
        verify(playerContext).setCurrPlayer(previousPlayer);
    }

    @Test
    void notify_callsSetCurrPlayerInCorrectOrder() {
        when(playerContext.getCurrPlayer()).thenReturn(previousPlayer);

        notificator.attach(cardOwner, strategy);
        notificator.notify(playerContext);

        var inOrder = inOrder(playerContext, strategy);
        inOrder.verify(playerContext).getCurrPlayer();
        inOrder.verify(playerContext).setCurrPlayer(cardOwner);
        inOrder.verify(strategy).effect(playerContext);
        inOrder.verify(playerContext).setCurrPlayer(previousPlayer);
    }
}