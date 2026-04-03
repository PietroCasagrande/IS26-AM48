package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShowedTest {
    private Showed<Card> showed;
    private Player player;

    @BeforeEach
    void setUp(){
        showed = new Showed<>();
        player = new Player("Ilaria", Totem.RED);
    }

    // addUpperCards() and addLowerCards()
    // adding cards to upper should make them accessible
    @Test
    void addUpperCardsShouldPopulateUpperList(){
        List<Card> cards = List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2)
        );
        showed.addUpperCards(cards);
        assertEquals(2, showed.getUpperList().size());
        assertEquals(0, showed.getLowerList().size());
    }

    // Adding cards to lower should make them accessible
    @Test
    void addLowerCardsShouldPopulateLowerList(){
        List<Card> cards = List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)
        );
        showed.addLowerCards(cards);
        assertEquals(0, showed.getUpperList().size());
        assertEquals(1, showed.getLowerList().size());
    }

    // Adding cards multiple times should accumulate
    @Test
    void addUpperCardsShouldAccumulate() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addUpperCards(List.of(new CharacterCard("H2", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertEquals(2, showed.getUpperList().size());
    }


    // shiftRow()
    // shiftRow should move all the cards from the upperList to the lowerList and clear upper
    @Test
    void ShiftRowShouldMoveUpperToLower(){
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2)
        ));
        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());
    }

    // shiftRow should append to existing lower cards
    @Test
    void shiftRowShouldAppendToExistingLower() {
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.SECOND, null, CharacterType.HUNTER, 2)));
        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());
    }


    // clearBottom()
    // clearBottom should empty the lower list
    @Test
    void ClearBottomShouldEmptyLowerList(){
        showed.addLowerCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)
        ));
        showed.clearBottom();
        assertEquals(0, showed.getLowerList().size());
    }

    // clearBottom should not affect upper list
    @Test
    void clearBottomShouldNotAffectUpperList() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.clearBottom();
        assertEquals(1, showed.getUpperList().size());
        assertEquals(0, showed.getLowerList().size());
    }


    // isTop() and isDown()
    // isTop should return true for cards in upper list
    @Test
    void isTopShouldReturnTrueForUpperCard() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.isTop("H1"));
        assertFalse(showed.isDown("H1"));
    }

    // isDown should return true for cards in lower list
    @Test
    void isDownShouldReturnTrueForLowerCard() {
        showed.addLowerCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.isDown("H1"));
        assertFalse(showed.isTop("H1"));
    }

    // both should return false for nonexistent card
    @Test
    void isTopAndIsDownShouldReturnFalseForMissingCard() {
        assertFalse(showed.isTop("NONEXISTENT"));
        assertFalse(showed.isDown("NONEXISTENT"));
    }


    // diffLastEras()
    // same era in both lists should return false
    @Test
    void diffLastErasShouldReturnFalseWhenSameEra() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    // different eras should return true
    @Test
    void diffLastErasShouldReturnTrueWhenDifferentEras() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.SECOND, null, CharacterType.HUNTER, 2)));
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertTrue(showed.diffLastEras());
    }

    // empty upper list should return false (no era to compare)
    @Test
    void diffLastErasShouldReturnFalseWhenUpperEmpty() {
        showed.addLowerCards(List.of(new CharacterCard("L1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    // empty lower list should return false (no era to compare)
    @Test
    void diffLastErasShouldReturnFalseWhenLowerEmpty() {
        showed.addUpperCards(List.of(new CharacterCard("U1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        assertFalse(showed.diffLastEras());
    }

    // both lists empty should return false
    @Test
    void diffLastErasShouldReturnFalseWhenBothEmpty() {
        assertFalse(showed.diffLastEras());
    }


    // takeCard() on a CharacterCard (always succeeds)
    // taking a character from upper should remove it and add to tribe
    @Test
    void takeCharacterFromUpperShouldRemoveAndAcquire() {
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2)
        ));
        Optional<Card> taken = showed.takeCard(player, "H1");
        assertTrue(taken.isPresent());
        assertEquals("H1", taken.get().getCardId());
        assertEquals(1, showed.getUpperList().size());
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    // taking a character from lower should remove it and add to tribe
    @Test
    void takeCharacterFromLowerShouldRemoveAndAcquire() {
        showed.addLowerCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)
        ));
        Optional<Card> taken = showed.takeCard(player, "H1");
        assertTrue(taken.isPresent());
        assertEquals(0, showed.getLowerList().size());
        assertEquals(1, player.getTribe().countByType(CharacterType.HUNTER));
    }

    // taking a nonexistent card should return empty
    @Test
    void takeCardNotFoundShouldReturnEmpty() {
        showed.addUpperCards(List.of(new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2)));
        Optional<Card> taken = showed.takeCard(player, "NONEXISTENT");
        assertTrue(taken.isEmpty());
        assertEquals(1, showed.getUpperList().size());
    }

    // takeCard() on a BuildingCard (might fail)
    // taking a building with enough food should succeed
    @Test
    void takeBuildingWithEnoughFoodShouldSucceed() {
        Showed<BuildingCard> buildingShowed = new Showed<>();
        buildingShowed.addUpperCards(List.of(new BuildingCard("B1", Era.FIRST, null, 3, 5)));
        player.updateFood(5);
        Optional<BuildingCard> taken = buildingShowed.takeCard(player, "B1");
        assertTrue(taken.isPresent());
        assertEquals(0, buildingShowed.getUpperList().size());
        assertEquals(2, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
    }

    // taking a building without enough food should fail and leave showed unchanged
    @Test
    void takeBuildingWithoutFoodShouldFailAndNotRemove() {
        Showed<BuildingCard> buildingShowed = new Showed<>();
        buildingShowed.addUpperCards(List.of(new BuildingCard("B1", Era.FIRST, null, 3, 5)));
        player.updateFood(1);
        assertThrows(InvalidActionException.class, () -> buildingShowed.takeCard(player, "B1"));
        assertEquals(1, buildingShowed.getUpperList().size());
        assertEquals(1, player.getFood());
        assertEquals(0, player.getTribe().getBuildings().size());
    }

    // takeCard() on an EventCard
    // taking an event card should fail because EventCard.acquire always throws
    @Test
    void takeEventCardShouldFailAndNotRemove() {
        showed.addUpperCards(List.of(
                new EventCard("EV1", Era.FIRST, null, EventType.HUNTER_EVENT)
        ));
        assertThrows(InvalidActionException.class, () -> showed.takeCard(player, "EV1"));
        assertEquals(1, showed.getUpperList().size());
    }


    // full turn simulation: add cards, take some, shift, clear
    @Test
    void fullRoundSimulation() {
        // Setup: 3 cards in upper
        showed.addUpperCards(List.of(
                new CharacterCard("H1", Era.FIRST, null, CharacterType.HUNTER, 2),
                new CharacterCard("H2", Era.FIRST, null, CharacterType.SHAMAN, 2),
                new CharacterCard("H3", Era.FIRST, null, CharacterType.BUILDER, 2)
        ));

        // Player takes one card from upper
        showed.takeCard(player, "H2");
        assertEquals(2, showed.getUpperList().size());
        assertEquals(1, player.getTotalCharacters());

        // End of round: shift remaining upper to lower
        showed.shiftRow();
        assertEquals(0, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());

        // New round: add new cards to upper
        showed.addUpperCards(List.of(
                new CharacterCard("H4", Era.SECOND, null, CharacterType.ARTIST, 2)
        ));
        assertEquals(1, showed.getUpperList().size());
        assertEquals(2, showed.getLowerList().size());

        // Era changed
        assertTrue(showed.diffLastEras());
    }

}