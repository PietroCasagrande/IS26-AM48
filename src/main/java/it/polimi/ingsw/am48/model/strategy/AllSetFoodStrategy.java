package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class AllSetFoodStrategy extends CardStrategy {
    private int completedSets = 0;

    public AllSetFoodStrategy(RegistrationAction registrationAction) {
        super(registrationAction);
    }

    @Override
    public void registerTo(NotificatorCenter notificatorCenter, PlayerContext playerContext) {
        // inizializza il counter al numero di set già presenti al momento dell'acquisizione
        completedSets = playerContext.getCurrPlayer().getTribe().minListSize();
        super.registerTo(notificatorCenter,playerContext);
    }

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
