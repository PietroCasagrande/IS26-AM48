package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class DoubleBuilderPPStrategy extends CardStrategy {

    public DoubleBuilderPPStrategy(RegistrationAction registration) {
        super(registration);
    }

    @Override
    public void effect(PlayerContext playerContext) {
        Player player = playerContext.getCurrPlayer();
        player.updateBuilderPoints(player.getBuilderPoints());
    }
}
