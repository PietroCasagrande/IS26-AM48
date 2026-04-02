package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.PlayerContext;

public class BuilderStrategy extends CardStrategy {
    private final int builderPp;
    private final int buildingDiscount;

    public BuilderStrategy(int builderPp, int buildingDiscount, RegistrationAction registrationAction, UnregistrationAction unregistrationAction) {
        super(registrationAction, unregistrationAction);
        this.builderPp = builderPp;
        this.buildingDiscount = buildingDiscount;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().updateBuilderPoints(builderPp);    // punti assegnati dai builder a fine partita
        playerContext.getCurrPlayer().updateBuildingDiscount(buildingDiscount);    // sconto (positivo) dato dai builder
    }
}
