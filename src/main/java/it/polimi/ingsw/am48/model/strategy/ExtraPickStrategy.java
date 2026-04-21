package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ExtraPickStrategy extends CardStrategy {
    public ExtraPickStrategy(RegistrationAction registration) {
        super(registration);
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setExtraPickRight();
    }
}
