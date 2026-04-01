package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class InventorsPairStrategy extends CardStrategy {
    int completedPairs;

    protected InventorsPairStrategy(RegistrationAction registration) {
        super(registration);
        completedPairs = 0;
    }

    @Override
    public void registerTo(NotificatorCenter notificatorCenter, PlayerContext playerContext){
        // inizializza il counter al numero di coppie già presenti al momento dell'acquisizione
        completedPairs = playerContext.getCurrPlayer().getTribe().countInventorPairs();
        super.registerTo(notificatorCenter,playerContext);
    }

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
