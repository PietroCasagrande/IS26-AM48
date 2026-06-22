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
        // initialize the counter to the number of sets already present at the time of acquisition
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
        // (at each OnPick) we check whether a new set has been completed, that is whether a character of the type that had the fewest has been added to the map
        // if so, we add 5 food to the currentFood of the player's tribe and update
        Player player = playerContext.getCurrPlayer();
        int newMin = player.getTribe().minListSize();
        if (newMin > completedSets) {
            player.updateFood((newMin - completedSets) * 5);    // note: newMin - completedSets should be at most 1 if we call it at each pickCard
            completedSets = newMin;
        }
    }
}
