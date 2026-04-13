package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class HuntEventStrategy extends CardStrategy {
    // food sempre +1, i pp cambiano
    private int ppPerPlayer;

    protected HuntEventStrategy(int ppPerPlayer, RegistrationAction registration) {
        super(registration);
        this.ppPerPlayer = ppPerPlayer;
    }

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
