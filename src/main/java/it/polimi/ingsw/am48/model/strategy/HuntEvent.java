package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class HuntEvent extends CardStrategy {
    // food sempre +1, i pp cambiano
    private int ppPerPlayer;

    protected HuntEvent(int ppPerPlayer, RegistrationAction registration, UnregistrationAction unregistration) {
        super(registration, unregistration);
        this.ppPerPlayer = ppPerPlayer;
    }

    // se si cambia il discorso di PlayerContext si può semplicemente chiamare new ResourcePerCharStrategy(FOOD_POINTS, ...)
    // e poi strategy.effect(player) per ogni player
    @Override
    public void effect(PlayerContext playerContext) {
        playerContext.getPlayers()
                .forEach(p -> {
                    int hunterCount = p.getTribe().countByType(CharacterType.HUNTER);
                    p.updateFood(hunterCount);
                    p.updatePoints(ppPerPlayer * hunterCount);
                });
    }
}
