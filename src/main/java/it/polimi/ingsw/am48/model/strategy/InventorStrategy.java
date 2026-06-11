package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.player.PlayerContext;

/**
 * Immediate effect of the Inventor character.
 * <p>
 * When acquired, the inventor adds its specific {@link Artifact} to the current player.
 *
 * @see CardStrategy
 * @see InventorsPairStrategy
 */
public class InventorStrategy extends CardStrategy{

    private final Artifact artifact;

    /**
     * Creates a new inventor effect.
     *
     * @param artifact            the artifact this inventor brings to the player
     * @param registrationAction  the registration action; a no-op for this immediate effect
     */
    public InventorStrategy(Artifact artifact, RegistrationAction registrationAction) {
        super(registrationAction);
        this.artifact = artifact;
    }

    /**
     * Adds this inventor's artifact to the current player.
     *
     * @param playerContext the context of the player who acquired the inventor
     */
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getCurrPlayer().addArtifact(artifact);
    }
}