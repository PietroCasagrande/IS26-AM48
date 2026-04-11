package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateResourcesStrategyTest {

    private Player currPlayer;
    private Player otherPlayer;
    private PlayerContext context;

    @BeforeEach
    void setUp() {
        currPlayer = new Player("alice", Totem.BLACK);
        otherPlayer = new Player("bob", Totem.BLUE);

        context = new PlayerContext();
        context.setCurrPlayer(currPlayer);
        context.addPlayer(currPlayer);
        context.addPlayer(otherPlayer);
    }

    @Test
    void shouldUpdateFoodCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD, 3, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFood());
    }

    @Test
    void shouldUpdateShamanStarsCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.STAR, 2, null);
        strategy.effect(context);
        assertEquals(2, currPlayer.getShamanStars());
    }

    @Test
    void shouldUpdateFoodDiscountCorrectly() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD_DISCOUNT, 3, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFoodDiscount());
    }

    @Test
    void shouldUpdatePrestigePointsBasedOnMinSetSize() {
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("Bu1", Era.FIRST, null, CharacterType.BUILDER, 2));
        currPlayer.addToTribe(new CharacterCard("I1", Era.FIRST, null, CharacterType.INVENTOR, 2));
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("P1", Era.FIRST, null, CharacterType.PICKER, 2));
        currPlayer.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.PRESTIGE_POINT, 3, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getPoints());
    }

    @Test
    void shouldGiveZeroPrestigePointsIfSetNotCompleted() {
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("Bu1", Era.FIRST, null, CharacterType.BUILDER, 2));
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.PRESTIGE_POINT, 3, null);
        strategy.effect(context);
        assertEquals(0, currPlayer.getPoints());
    }

    @Test
    void shouldMultiplyPrestigePointsByCompletedSets() {
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("A2", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("Bu1", Era.FIRST, null, CharacterType.BUILDER, 2));
        currPlayer.addToTribe(new CharacterCard("Bu2", Era.FIRST, null, CharacterType.BUILDER, 2));
        currPlayer.addToTribe(new CharacterCard("I1", Era.FIRST, null, CharacterType.INVENTOR, 2));
        currPlayer.addToTribe(new CharacterCard("I2", Era.FIRST, null, CharacterType.INVENTOR, 2));
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("P1", Era.FIRST, null, CharacterType.PICKER, 2));
        currPlayer.addToTribe(new CharacterCard("P2", Era.FIRST, null, CharacterType.PICKER, 2));
        currPlayer.addToTribe(new CharacterCard("S1", Era.FIRST, null, CharacterType.SHAMAN, 2));
        currPlayer.addToTribe(new CharacterCard("S2", Era.FIRST, null, CharacterType.SHAMAN, 2));
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.PRESTIGE_POINT, 6, null);
        strategy.effect(context);
        assertEquals(12, currPlayer.getPoints());
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        UpdateResourcesStrategy strategy = new UpdateResourcesStrategy(Resource.FOOD, 3, null);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}
