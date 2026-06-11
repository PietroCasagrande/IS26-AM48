package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * The abstract base of the Strategy pattern used to model every card effect in the game.
 * <p>
 * Cards of the same Java type (Characters, Buildings, Events) can carry very different
 * abilities; rather than subclassing each card, the concrete behavior is delegated to a
 * {@code CardStrategy}. Each strategy combines two responsibilities: the {@link #effect}
 * to be executed (what the card does) and a {@link RegistrationAction} (when the effect
 * is triggered). Immediate, "one-shot" effects (typically Characters) resolve as soon as
 * they are invoked, while delayed effects (typically Buildings and Events) register
 * themselves as listeners on a notificator and fire later, at a specific game phase.
 *
 * @see RegistrationAction
 * @see NotificatorCenter
 */
public abstract class CardStrategy {
    private final RegistrationAction registration;

    /**
     * Creates a new strategy bound to the given registration action.
     *
     * @param registration the action describing where and how this strategy attaches
     *                      itself as a listener; may be a no-op lambda for immediate
     *                      effects that never need to register
     */
    public CardStrategy(RegistrationAction registration){
        this.registration = registration;
    }

    /**
     * Applies the concrete effect of this strategy to the game state.
     * <p>
     * Each subclass implements its own behavior here, modifying the current player,
     * all players or their tribes as required by the card's ability.
     *
     * @param playerContext the context granting access to the current player and to the
     *                       full list of players involved in the effect
     */
    public abstract void effect(PlayerContext playerContext);

    /**
     * Registers this strategy as a listener so that its {@link #effect} can be triggered
     * later by the appropriate notificator.
     * <p>
     * The actual attachment is delegated to the {@link RegistrationAction} provided at
     * construction time. If no registration action is present, the call is a no-op,
     * which is the case for immediate effects that are resolved on the spot.
     *
     * @param nc            the notificator center exposing the available trigger channels
     * @param playerContext the context of the player owning the card whose effect is registered
     */
    public void registerTo(NotificatorCenter nc, PlayerContext playerContext){
        if(registration != null){
            registration.registerMethod(nc, playerContext, this);
        }
    }
}
