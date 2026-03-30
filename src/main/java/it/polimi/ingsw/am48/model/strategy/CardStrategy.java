package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

public abstract class CardStrategy {
    private final RegistrationAction registration;

    // da rivedere sta cosa strana
    protected CardStrategy(RegistrationAction registration){
        this.registration = registration;
    }

    public abstract void effect(PlayerContext playerContext);

    public void registerTo(NotificatorCenter nc, Player player){
        if(registration != null){
            registration.registerMethod(nc, player, this);
        }
    }

    public void unregisterFrom(List<CardStrategy> toDetach){
        if(registration != null){
            registration.unregisterMethod(toDetach);
        }
    }
}
