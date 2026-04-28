package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.BuildingCard;
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

class ResourcePerCharStrategyTest {

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
    void shouldUpdateFoodBasedOnCharacterCount() {
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD, 2, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(4, currPlayer.getFood()); // 2 food × 2 hunters
    }

    @Test
    void shouldUpdateFoodDiscountBasedOnCharacterCount() {
        currPlayer.addToTribe(new CharacterCard("P1", Era.FIRST, null, CharacterType.PICKER, 2));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD_DISCOUNT, 3, CharacterType.PICKER, null);
        strategy.effect(context);
        assertEquals(3, currPlayer.getFoodDiscount()); // 3 discount × 1 picker
    }

    @Test
    void shouldUpdatePrestigePointsBasedOnCharacterCount() {
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("H3", Era.FIRST, null, CharacterType.HUNTER, 2));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.PRESTIGE_POINT, 4, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(12, currPlayer.getPoints()); // 4 pp × 3 hunters
    }

    @Test
    void shouldGiveZeroIfNoMatchingCharacters() {
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD, 2, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood()); // nessun hunter, nessun cibo
    }

    @Test
    void shouldIgnoreCharactersOfOtherTypes() {
        // verifica che countByType filtri correttamente solo il characterType richiesto
        currPlayer.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        currPlayer.addToTribe(new CharacterCard("A2", Era.FIRST, null, CharacterType.ARTIST, 2));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD, 2, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood()); // ha artisti, non hunter
    }

    @Test
    void shouldUpdatePrestigePointsFromBuildingBasedOnHunterCount() {
        // edificio che dà 3 pp per ogni hunter posseduto
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2));
        currPlayer.addToTribe(new BuildingCard("B1", Era.FIRST, null, 3, 0));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.PRESTIGE_POINT, 3, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(6, currPlayer.getPoints()); // 3 pp × 2 hunters, l'edificio non conta
    }

    @Test
    void shouldNotCountBuildingsAsCharacters() {
        // verifica che gli edifici in tribù non vengano contati come personaggi
        currPlayer.addToTribe(new BuildingCard("B1", Era.FIRST, null, 3, 0));
        currPlayer.addToTribe(new BuildingCard("B2", Era.FIRST, null, 2, 0));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD, 2, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(0, currPlayer.getFood()); // edifici non contano come hunter
    }

    @Test
    void shouldThrowOnInvalidResource() {
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.STAR, 1, CharacterType.HUNTER, null);
        assertThrows(IllegalArgumentException.class, () -> strategy.effect(context));
    }

    @Test
    void shouldNotAffectOtherPlayers() {
        currPlayer.addToTribe(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2));
        ResourcePerCharStrategy strategy = new ResourcePerCharStrategy(Resource.FOOD, 2, CharacterType.HUNTER, null);
        strategy.effect(context);
        assertEquals(0, otherPlayer.getFood());
    }
}