package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

public class DoubleShamanPPStrategy extends CardStrategy {
    protected DoubleShamanPPStrategy(RegistrationAction registration, UnregistrationAction unregistrationAction) {
        super(registration, unregistrationAction);
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setShamanDoubling();
    }
}
