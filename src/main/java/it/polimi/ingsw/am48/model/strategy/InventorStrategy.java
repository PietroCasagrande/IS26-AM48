package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class InventorStrategy extends CardStrategy{

    private final Artifact artifact;

    public InventorStrategy(Artifact artifact, RegistrationAction registrationAction) {
        super(registrationAction);
        this.artifact = artifact;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().addArtifact(artifact);
    }
}