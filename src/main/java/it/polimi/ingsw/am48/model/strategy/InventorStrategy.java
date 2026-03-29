package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;

public class InventorStrategy extends CardStrategy{

    private final Artifact artifact;

    public InventorStrategy(RegistrationAction ra, Artifact artifact) {
        super(ra);
        this.artifact = artifact;
    }

    @Override
    public void effect(Player player, List<Player> allPlayers) {
        player.updateArtifacts(artifact);
    }
}
