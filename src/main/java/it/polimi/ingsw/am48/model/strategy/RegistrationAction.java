package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Functional interface describing the trigger binding of a {@link CardStrategy}.
 * <p>
 * A registration action decouples <em>what</em> an effect does from <em>when</em> it
 * fires: it is the lambda, supplied by the factories, that attaches a strategy to the
 * proper notification channel (e.g. on pick, on a given event, at the end of the game).
 * Immediate effects are configured with an empty implementation, since they are resolved
 * at once and never need to listen for a future trigger.
 *
 * @see CardStrategy
 * @see NotificatorCenter
 */
public interface RegistrationAction {
    /**
     * Attaches the given strategy to the correct notificator channel.
     *
     * @param nc            the notificator center exposing the available trigger channels
     * @param playerContext the context of the player owning the card being registered
     * @param strategy      the strategy instance to attach as a listener
     */
    void registerMethod(NotificatorCenter nc, PlayerContext playerContext, CardStrategy strategy);
}
