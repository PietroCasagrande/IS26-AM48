package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Building effect that rewards the player with food whenever a new inventor pair is formed.
 * <p>
 * A pair is made of two inventors carrying the same artifact. Registered on the pick
 * channel, this effect checks at each pick whether the number of matching inventor pairs
 * grew and, if so, grants 3 food per newly completed pair. The internal counter is seeded
 * at registration time with the pairs already owned, so only pairs formed afterwards are
 * rewarded.
 *
 * @see CardStrategy
 * @see InventorStrategy
 */
public class InventorsPairStrategy extends CardStrategy {
    int completedPairs;

    /**
     * Creates a new inventor-pair food effect.
     *
     * @param registration the registration action attaching this effect to the pick channel
     */
    public InventorsPairStrategy(RegistrationAction registration) {
        super(registration);
        completedPairs = 0;
    }

    /**
     * Seeds the completed-pair counter with the pairs already present when the building is
     * acquired, then performs the standard registration.
     *
     * @param notificatorCenter the notificator center exposing the trigger channels
     * @param playerContext     the context of the player acquiring the building
     */
    @Override
    public void registerTo(NotificatorCenter notificatorCenter, PlayerContext playerContext){
        // inizializza il counter al numero di coppie già presenti al momento dell'acquisizione
        completedPairs = playerContext.getCurrPlayer().getTribe().countInventorPairs();
        super.registerTo(notificatorCenter,playerContext);
    }

    /**
     * Grants 3 food for each inventor pair completed since the last check.
     *
     * @param playerContext the context of the current player
     */
    @Override
    public void effect(PlayerContext playerContext) {
        // ON PICK controlla se nella tribe è cambiato il numero di coppie di inventori con lo stesso artifact
        // in caso affermativo assegna 3 cibo al player
        Player player = playerContext.getCurrPlayer();
        int newPairs = player.getTribe().countInventorPairs();
        if (newPairs > completedPairs) {
            player.updateFood((newPairs - completedPairs) * 3);
            completedPairs = newPairs;
        }
    }

}
