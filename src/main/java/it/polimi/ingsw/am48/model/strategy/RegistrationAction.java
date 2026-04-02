package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.PlayerContext;


public interface RegistrationAction {
    void registerMethod(NotificatorCenter nc, PlayerContext playerContext, CardStrategy strategy);
}
