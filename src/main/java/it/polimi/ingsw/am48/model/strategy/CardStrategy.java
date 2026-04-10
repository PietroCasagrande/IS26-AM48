package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

public abstract class CardStrategy {
    private final RegistrationAction registration;

    protected CardStrategy(RegistrationAction registration){
        this.registration = registration;
    }

    public abstract void effect(PlayerContext playerContext);

    public void registerTo(NotificatorCenter nc, PlayerContext playerContext){
        if(registration != null){
            registration.registerMethod(nc, playerContext, this);
        }
    }
}
