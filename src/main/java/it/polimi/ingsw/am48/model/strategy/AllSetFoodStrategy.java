package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that rewards the player with food whenever a new complete character set
 * is formed.
 * <p>
 * A "set" is one character of every type; the number of complete sets equals the size of
 * the smallest per-type list in the tribe ({@code minListSize}). This effect is registered
 * on the pick channel: at each pick it checks whether the set count grew and, if so, grants
 * 5 food per newly completed set. The internal counter is seeded at registration time with
 * the sets already owned, so only sets formed afterwards are rewarded.
 *
 * @see CardStrategy
 */
public class AllSetFoodStrategy extends CardStrategy {
    private int completedSets = 0;

    /**
     * Creates a new all-set food effect.
     *
     * @param registrationAction the registration action attaching this effect to the pick channel
     */
    public AllSetFoodStrategy(RegistrationAction registrationAction) {
        super(registrationAction);
    }

    /**
     * Seeds the completed-set counter with the sets already present when the building is
     * acquired, then performs the standard registration.
     *
     * @param notificatorCenter the notificator center exposing the trigger channels
     * @param playerContext     the context of the player acquiring the building
     */
    @Override
    public void registerTo(NotificatorCenter notificatorCenter, PlayerContext playerContext) {
        // inizializza il counter al numero di set già presenti al momento dell'acquisizione
        completedSets = playerContext.getCurrPlayer().getTribe().minListSize();
        super.registerTo(notificatorCenter,playerContext);
    }

    /**
     * Grants 5 food for each character set completed since the last check.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        // (ad ogni OnPick) verifichiamo se è stato completato un nuovo set, ovvero se è stato aggiunto alla map un character del tipo che ne aveva meno
        // in caso affermativo aggiungiamo 5 food al currentFood della tribe del player e aggiorniamo
        Player player = playerContext.getCurrPlayer();
        int newMin = player.getTribe().minListSize();
        if (newMin > completedSets) {
            player.updateFood((newMin - completedSets) * 5);    // oss: newMin - completedSets dovrebbe essere al max 1 se lo chiamiamo ad ogni pickCard
            completedSets = newMin;
        }
    }
}
