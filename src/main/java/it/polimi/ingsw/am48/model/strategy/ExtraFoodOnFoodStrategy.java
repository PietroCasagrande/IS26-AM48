package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ExtraFoodOnFoodStrategy extends CardStrategy {

    public ExtraFoodOnFoodStrategy(RegistrationAction registration) {
        super(registration);
    }

    @Override
    public void effect(PlayerContext playerContext) {
        // diamo +1 cibo extra solo se il player è in una delle caselle di OfferTurnCard che dà cibo
        Player player = playerContext.getCurrPlayer();
        if(player.deservesExtraFood()) player.updateFood(1);
    }
}
