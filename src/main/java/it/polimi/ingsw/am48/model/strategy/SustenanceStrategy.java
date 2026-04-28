package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class SustenanceStrategy extends CardStrategy {
    private int ppLostPerChar;    // salvato con segno POSITIVO

    public SustenanceStrategy(int ppLostPerChar, RegistrationAction registration) {
        super(registration);
        this.ppLostPerChar = ppLostPerChar;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getPlayers().forEach(p -> {
            int foodToPay = p.getTotalCharacters() - p.getFoodDiscount();
            if (foodToPay > 0) p.payFood(foodToPay, ppLostPerChar);
        });
    }
}
