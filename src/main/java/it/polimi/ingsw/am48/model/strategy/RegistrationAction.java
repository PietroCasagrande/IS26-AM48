package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;

public interface RegistrationAction {
    void registerMethod(NotificatorCenter nc, Player player, CardStrategy strategy);
    void unregisterMethod(List<CardStrategy> toDetach);
}
