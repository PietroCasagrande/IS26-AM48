package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ResourcePerCharStrategy extends CardStrategy{
    private final Resource resource;
    private final int quantity;
    private final CharacterType characterType;

    public ResourcePerCharStrategy(Resource resource, int quantity, CharacterType characterType, RegistrationAction registration) {
        // PER IL MOMENTO SEGNATA COME PERSISTENTE, MA DA RIVEDERE PER L'HUNTER CHARACTER
        super(registration);
        this.resource = resource;
        this.quantity = quantity;
        this.characterType = characterType;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        int num = playerContext.getCurrPlayer().getTribe().countByType(characterType);
        int amount = quantity * num;
        switch (resource) {
            case FOOD -> playerContext.getCurrPlayer().updateFood(amount);    // hunter character
            case FOOD_POINTS -> {    // edificio 7 (1 food e 1 pp per hunter) e forse chiamato da HuntEvent
                playerContext.getCurrPlayer().updateFood(num);    // food aggiornato è sempre +1
                playerContext.getCurrPlayer().updatePoints(amount);    // pp aggiornati sono 1 per edificio 7 ma 1-3 per evento caccia (in caso si voglia chiamare questa strategy iterativamente dentro HuntEvent)
            }
            case FOOD_DISCOUNT ->  playerContext.getCurrPlayer().updateFoodDiscount(amount);    // edifici sconto carestia
            case PRESTIGE_POINT -> playerContext.getCurrPlayer().updatePoints(amount);    // edifici pp a fine game
            default -> throw new IllegalArgumentException("Invalid resource " + resource);
        }
    }
}
