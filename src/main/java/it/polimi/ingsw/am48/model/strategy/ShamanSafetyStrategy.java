package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ShamanSafetyStrategy extends CardStrategy {

    protected ShamanSafetyStrategy(RegistrationAction registration) {
        super(registration);
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().setShamanSafety();
    }
}
