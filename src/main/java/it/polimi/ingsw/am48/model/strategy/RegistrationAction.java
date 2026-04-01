package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

public interface RegistrationAction {
    void registerMethod(NotificatorCenter nc, PlayerContext playerContext, CardStrategy strategy);
    void unregisterMethod(List<CardStrategy> toDetach);
}
