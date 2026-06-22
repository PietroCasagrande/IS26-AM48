package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.CardDTO;
import it.polimi.ingsw.am48.dto.StrategyDTO;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.enums.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingFactoryTest {

    private CardDTO dtoNoStrategy;

    @BeforeEach
    void setUp() {
        dtoNoStrategy = new CardDTO();
        dtoNoStrategy.id = "BLD-20";
        dtoNoStrategy.era = "THIRD";
        dtoNoStrategy.foodCost = 10;
        dtoNoStrategy.prestigePoints = 25;
        dtoNoStrategy.strategy = null;
    }

    // Helper to create a DTO with a strategy
    private CardDTO buildDto(String id, String era, String effect, String notificator) {
        StrategyDTO s = new StrategyDTO();
        s.effect = effect;
        s.notificator = notificator;
        CardDTO dto = new CardDTO();
        dto.id = id;
        dto.era = era;
        dto.foodCost = 3;
        dto.prestigePoints = 3;
        dto.strategy = s;
        return dto;
    }

    private CardDTO buildDtoWithEventType(String id, String era, String effect,
                                          String notificator, String eventType) {
        CardDTO dto = buildDto(id, era, effect, notificator);
        dto.strategy.eventType = eventType;
        return dto;
    }

    private CardDTO buildDtoWithResource(String id, String era, String effect,
                                         String notificator, String resource,
                                         String character, int num1) {
        CardDTO dto = buildDto(id, era, effect, notificator);
        dto.strategy.resource = resource;
        dto.strategy.character = character;
        dto.strategy.num1 = num1;
        return dto;
    }

    private CardDTO buildDtoWithResourceAndEventType(String id, String era, String effect,
                                                     String notificator, String eventType,
                                                     String resource, String character, int num1) {
        CardDTO dto = buildDtoWithResource(id, era, effect, notificator, resource, character, num1);
        dto.strategy.eventType = eventType;
        return dto;
    }

    // ==================== createCards - validation ====================

    @Test
    @DisplayName("createCards: should return a BuildingCard for each DTO provided")
    void shouldCreateOneCardPerDto() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy, dtoNoStrategy));
        assertEquals(2, factory.createCards(3).size());
    }

    @Test
    @DisplayName("createCards: should map card id correctly from DTO")
    void shouldMapCardIdCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy));
        assertEquals("BLD-20", factory.createCards(2).getFirst().getCardId());
    }

    @Test
    @DisplayName("createCards: should map era correctly from DTO")
    void shouldMapEraCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy));
        assertEquals(Era.THIRD, factory.createCards(2).getFirst().getEra());
    }

    @Test
    @DisplayName("createCards: should map foodCost correctly from DTO")
    void shouldMapFoodCostCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy));
        assertEquals(10, factory.createCards(2).getFirst().getFoodCost());
    }

    @Test
    @DisplayName("createCards: should map prestigePoints correctly from DTO")
    void shouldMapPrestigePointsCorrectly() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy));
        assertEquals(25, factory.createCards(2).getFirst().getPrestigePoints());
    }

    @Test
    @DisplayName("createCards: should set strategy to null when DTO has no strategy")
    void shouldSetNullStrategyWhenDtoStrategyIsNull() {
        BuildingFactory factory = new BuildingFactory(List.of(dtoNoStrategy));
        assertNull(factory.createCards(2).getFirst().getStrategy());
    }

    @Test
    @DisplayName("createCards: should return an empty list when given an empty DTO list")
    void shouldReturnEmptyListForEmptyDtoList() {
        assertTrue(new BuildingFactory(List.of()).createCards(2).isEmpty());
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers below minimum")
    void shouldThrowForNumPlayersBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new BuildingFactory(List.of(dtoNoStrategy)).createCards(1));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for numPlayers above maximum")
    void shouldThrowForNumPlayersAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> new BuildingFactory(List.of(dtoNoStrategy)).createCards(6));
    }

    @Test
    @DisplayName("createCards: should accept minimum valid numPlayers (2)")
    void shouldAcceptMinimumValidNumPlayers() {
        assertDoesNotThrow(() -> new BuildingFactory(List.of(dtoNoStrategy)).createCards(2));
    }

    @Test
    @DisplayName("createCards: should accept maximum valid numPlayers (5)")
    void shouldAcceptMaximumValidNumPlayers() {
        assertDoesNotThrow(() -> new BuildingFactory(List.of(dtoNoStrategy)).createCards(5));
    }

    @Test
    @DisplayName("createCards: should throw IllegalArgumentException for unknown era value")
    void shouldThrowForUnknownEraValue() {
        dtoNoStrategy.era = "INVALID_ERA";
        assertThrows(IllegalArgumentException.class,
                () -> new BuildingFactory(List.of(dtoNoStrategy)).createCards(2));
    }

    // ==================== buildStrategy - every branch of the switch ====================

    @Test
    @DisplayName("buildStrategy: should build ExtraFoodOnFoodStrategy with OnTotemReturned notificator")
    void shouldBuildExtraFoodOnFoodStrategy() {
        CardDTO dto = buildDto("BLD-01", "FIRST", "ExtraFoodOnFoodStrategy", "OnTotemReturned");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build InventorsPairStrategy with OnPick notificator")
    void shouldBuildInventorsPairStrategy() {
        CardDTO dto = buildDto("BLD-02", "FIRST", "InventorsPairStrategy", "OnPick");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ShamanSafetyStrategy with OnEvent notificator")
    void shouldBuildShamanSafetyStrategy() {
        CardDTO dto = buildDtoWithEventType("BLD-03", "FIRST",
                "ShamanSafetyStrategy", "OnEvent", "SHAMAN_EVENT");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build AllSetFoodStrategy with OnPick notificator")
    void shouldBuildAllSetFoodStrategy() {
        CardDTO dto = buildDto("BLD-04", "FIRST", "AllSetFoodStrategy", "OnPick");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build HuntEventStrategy with OnEvent notificator")
    void shouldBuildHuntEventStrategy() {
        CardDTO dto = buildDtoWithEventType("BLD-10", "SECOND",
                "HuntEventStrategy", "OnEvent", "HUNTER_EVENT");
        dto.strategy.num1 = 1;
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build DoubleBuilderPPStrategy with OnEndGame notificator")
    void shouldBuildDoubleBuilderPPStrategy() {
        CardDTO dto = buildDto("BLD-11", "SECOND", "DoubleBuilderPPStrategy", "OnEndGame");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build DoubleShamanPPStrategy with OnEvent notificator")
    void shouldBuildDoubleShamanPPStrategy() {
        CardDTO dto = buildDtoWithEventType("BLD-12", "SECOND",
                "DoubleShamanPPStrategy", "OnEvent", "SHAMAN_EVENT");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ExtraPickStrategy with OnEndOfferPhase notificator")
    void shouldBuildExtraPickStrategy() {
        CardDTO dto = buildDto("BLD-21", "THIRD", "ExtraPickStrategy", "OnEndOfferPhase");
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ResourcePerCharStrategy with OnEvent notificator")
    void shouldBuildResourcePerCharStrategyWithOnEvent() {
        CardDTO dto = buildDtoWithResourceAndEventType("BLD-05", "FIRST",
                "ResourcePerCharStrategy", "OnEvent", "PICKER_EVENT",
                "FOOD_DISCOUNT", "PICKER", 1);
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build ResourcePerCharStrategy with OnEndGame notificator")
    void shouldBuildResourcePerCharStrategyWithOnEndGame() {
        CardDTO dto = buildDtoWithResource("BLD-14", "THIRD",
                "ResourcePerCharStrategy", "OnEndGame",
                "PRESTIGE_POINT", "BUILDER", 4);
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build UpdateResourcesStrategy with OnEndGame notificator")
    void shouldBuildUpdateResourcesStrategyWithOnEndGame() {
        CardDTO dto = buildDto("BLD-09", "SECOND", "UpdateResourcesStrategy", "OnEndGame");
        dto.strategy.resource = "PRESTIGE_POINT";
        dto.strategy.num1 = 6;
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should build UpdateResourcesStrategy with OnPick notificator")
    void shouldBuildUpdateResourcesStrategyWithOnPick() {
        CardDTO dto = buildDto("BLD-13", "SECOND", "UpdateResourcesStrategy", "OnPick");
        dto.strategy.resource = "STAR";
        dto.strategy.num1 = 3;
        BuildingCard card = new BuildingFactory(List.of(dto)).createCards(2).getFirst();
        assertNotNull(card.getStrategy());
    }

    @Test
    @DisplayName("buildStrategy: should throw IllegalArgumentException for unknown strategy effect")
    void shouldThrowForUnknownStrategyEffect() {
        CardDTO dto = buildDto("BLD-XX", "FIRST", "UnknownStrategy", "OnPick");
        assertThrows(IllegalArgumentException.class,
                () -> new BuildingFactory(List.of(dto)).createCards(2));
    }

    @Test
    @DisplayName("buildRegistrationAction: should throw IllegalArgumentException for unknown notificator")
    void shouldThrowForUnknownNotificator() {
        CardDTO dto = buildDto("BLD-XX", "FIRST", "ExtraFoodOnFoodStrategy", "InvalidNotificator");
        assertThrows(IllegalArgumentException.class,
                () -> new BuildingFactory(List.of(dto)).createCards(2));
    }
}