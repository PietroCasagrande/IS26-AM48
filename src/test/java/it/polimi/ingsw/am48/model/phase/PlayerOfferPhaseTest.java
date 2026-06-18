package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.delta.*;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.notificator.*;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PlayerOfferPhaseSnapshot;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerOfferPhaseTest {

    private Game mockGame;
    private Board mockBoard;
    private NotificatorCenter mockNotificatorCenter;
    private OnPickNotificator mockPickNotificator;
    private OnTotemReturnedNotificator mockTotemReturnedNotificator;
    private OnEventNotificator mockEventNotificator;
    private OnEndGameNotificator mockEndGameNotificator;
    private Showed<Card> mockTribeShowed;
    private Showed<BuildingCard> mockBuildingShowed;
    private PlayerContext playerContext;

    private Player p1;
    private Player p2;
    private Player p3;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        p1 = new Player("alice", Totem.BLACK);
        p2 = new Player("bob", Totem.BLUE);
        p3 = new Player("charlie", Totem.RED);

        playerContext = new PlayerContext();
        playerContext.addPlayer(p1);
        playerContext.addPlayer(p2);
        playerContext.addPlayer(p3);

        mockGame = mock(Game.class);
        mockBoard = mock(Board.class);
        mockNotificatorCenter = mock(NotificatorCenter.class);
        mockPickNotificator = mock(OnPickNotificator.class);
        mockEndGameNotificator = mock(OnEndGameNotificator.class);
        mockTotemReturnedNotificator = mock(OnTotemReturnedNotificator.class);
        mockEventNotificator = mock(OnEventNotificator.class);
        mockTribeShowed = mock(Showed.class);
        mockBuildingShowed = mock(Showed.class);

        when(mockGame.getBoard()).thenReturn(mockBoard);
        when(mockGame.getNotificatorCenter()).thenReturn(mockNotificatorCenter);
        when(mockGame.getPlayerContext()).thenReturn(playerContext);
        when(mockNotificatorCenter.getPickNotificator()).thenReturn(mockPickNotificator);
        when(mockNotificatorCenter.getEndGameNotificator()).thenReturn(mockEndGameNotificator);
        when(mockNotificatorCenter.getTotemReturnedNotificator()).thenReturn(mockTotemReturnedNotificator);
        when(mockNotificatorCenter.getEventNotificator()).thenReturn(mockEventNotificator);
        when(mockBoard.getTribeShowed()).thenReturn(mockTribeShowed);
        when(mockBoard.getBuildingShowed()).thenReturn(mockBuildingShowed);
        when(mockTribeShowed.getUpperList()).thenReturn(List.of());
        when(mockTribeShowed.getLowerList()).thenReturn(List.of());
        when(mockBuildingShowed.getUpperList()).thenReturn(List.of());
        when(mockBuildingShowed.getLowerList()).thenReturn(List.of());
        when(mockGame.findWinner()).thenReturn(p1);
    }

    // ==================== helpers ====================

    private CharacterCard makeCharCard(String id) {
        return new CharacterCard(id, Era.FIRST, null, CharacterType.HUNTER, 2);
    }

    private BuildingCard makeBuildingCard(String id) {
        return new BuildingCard(id, Era.FIRST, null, 0, 5);
    }

    private OfferCard freshOfferB(Player player) {
        OfferCard offer = new OfferCard('B', 1, 0, 0, 3);
        offer.placeTotem(player);
        return offer;
    }

    private OfferCard freshOfferA(Player player) {
        OfferCard offer = new OfferCard('A', 1, 0, 3, 3);
        offer.placeTotem(player);
        return offer;
    }

    private OfferCard freshOfferC(Player player) {
        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(player);
        return offer;
    }

    private OfferCard setupNormalPick(Player player, char letter, int numUp, int numDown,
                                      int foodBonus, String cardId, Card card) {
        OfferCard offer = new OfferCard(letter, numUp, numDown, foodBonus, 3);
        offer.placeTotem(player);
        when(mockBoard.findTrackPosition(player)).thenReturn(offer);
        when(mockBoard.isCardTop(cardId)).thenReturn(true);
        when(mockBoard.isCardDown(cardId)).thenReturn(false);
        when(mockBoard.takeCard(any(PlayerContext.class), eq(cardId))).thenReturn(card);
        return offer;
    }

    private OfferCard setupNormalPickB(Player player, String cardId, Card card) {
        return setupNormalPick(player, 'B', 1, 0, 0, cardId, card);
    }

    private OfferCard setupNormalPickFromBottom(Player player, char letter, int numUp, int numDown,
                                                int foodBonus, String cardId, Card card) {
        OfferCard offer = new OfferCard(letter, numUp, numDown, foodBonus, 3);
        offer.placeTotem(player);
        when(mockBoard.findTrackPosition(player)).thenReturn(offer);
        when(mockBoard.isCardTop(cardId)).thenReturn(false);
        when(mockBoard.isCardDown(cardId)).thenReturn(true);
        when(mockBoard.takeCard(any(PlayerContext.class), eq(cardId))).thenReturn(card);
        return offer;
    }

    private void setupEndTurnMocks() {
        when(mockGame.getCurrentTurn()).thenReturn(1);
        when(mockBoard.getPlaceOrder()).thenReturn(List.of(p1, p2, p3));
    }

    // ==================== constructor (primary) ====================

    @Test
    @DisplayName("constructor: should not produce side effects")
    void shouldInitializeWithCorrectDefaults() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        assertEquals(0, p1.getFood());
        verify(mockBoard, never()).returnTotem(any());
    }

    // ==================== constructor (fromSnapshot) ====================

    @Test
    @DisplayName("fromSnapshot constructor: should restore state and allow the correct player to pick")
    void shouldRestoreStateFromSnapshotConstructor() {
        // currIdx=1 means p2 is next
        PlayerOfferPhase phase = new PlayerOfferPhase(
                List.of(p1, p2, p3), 1, 0, 0, false, null, false);

        Card card = makeCharCard("card1");
        setupNormalPickB(p2, "card1", card);

        assertDoesNotThrow(() -> phase.takeCard(mockGame, p2, "card1"));
    }

    @Test
    @DisplayName("fromSnapshot constructor: should restore extraPickActive and extraPickPlayer")
    void shouldRestoreExtraPickStateFromSnapshotConstructor() {
        // extraPickActive=true, extraPickPlayer=p2 → p3 cannot pick
        PlayerOfferPhase phase = new PlayerOfferPhase(
                List.of(p1, p2, p3), 3, 0, 0, true, p2, false);

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p3, "cardX"));
    }

    // ==================== setup ====================

    @Test
    @DisplayName("setup: should give food and return totem when first player is on card A")
    void shouldGiveFoodAndReturnTotemIfFirstPlayerOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferA(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        phase.setup(mockGame);
        assertEquals(3, p1.getFood());
        verify(mockBoard).returnTotem(p1);
    }

    @Test
    @DisplayName("setup: should return OfferCardADelta when first player is on card A")
    void shouldReturnOfferCardADeltaIfFirstPlayerOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferA(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Optional<GameDelta> result = phase.setup(mockGame);
        assertTrue(result.isPresent());
        assertInstanceOf(OfferCardADelta.class, result.get());
    }

    @Test
    @DisplayName("setup: should return empty Optional when first player is not on card A")
    void shouldReturnEmptyOptionalIfFirstPlayerNotOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferB(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Optional<GameDelta> result = phase.setup(mockGame);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("setup: should not give food when first player is not on card A")
    void shouldNotGiveFoodIfFirstPlayerNotOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferB(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        phase.setup(mockGame);
        assertEquals(0, p1.getFood());
    }

    @Test
    @DisplayName("setup: should not return totem when first player is not on card A")
    void shouldNotReturnTotemIfFirstPlayerNotOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferB(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        phase.setup(mockGame);
        verify(mockBoard, never()).returnTotem(any());
    }

    @Test
    @DisplayName("setup: should skip first player in action order when on card A")
    void shouldSkipFirstPlayerInActionOrderIfOnCardA() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferA(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        phase.setup(mockGame);
        Card card = makeCharCard("card1");
        setupNormalPickB(p2, "card1", card);
        assertDoesNotThrow(() -> phase.takeCard(mockGame, p2, "card1"));
    }

    @Test
    @DisplayName("setup: should include correct nickname in OfferCardADelta")
    void shouldHaveCorrectNicknameInOfferCardADelta() {
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferA(p1));
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Optional<GameDelta> result = phase.setup(mockGame);
        OfferCardADelta delta = (OfferCardADelta) result.get();
        assertEquals("alice", delta.getPlayerNickname());
    }

    @Test
    @DisplayName("setup: should not throw even when offerA totem is absent (orElse null path)")
    void shouldNotThrowInSetupWhenOfferATotemAbsent() {
        // OfferCard with letter A but no totem placed — returnTotem() returns empty
        OfferCard emptyOfferA = new OfferCard('A', 1, 0, 3, 3);
        when(mockBoard.findTrackPosition(p1)).thenReturn(emptyOfferA);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        assertDoesNotThrow(() -> phase.setup(mockGame));
        // handleTotemReturn is called with null — returnTotem(null) is called on board
        verify(mockBoard).returnTotem(null);
    }

    // ==================== takeCard - turn validation ====================

    @Test
    @DisplayName("takeCard: should throw InvalidActionException when it is not the player's turn")
    void shouldThrowWhenNotPlayersTurn() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "card1"));
    }

    @Test
    @DisplayName("takeCard: should allow the correct player to pick on their turn")
    void shouldAllowCorrectPlayerToPickOnTheirTurn() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        assertDoesNotThrow(() -> phase.takeCard(mockGame, p1, "card1"));
    }

    // ==================== takeCard - validatePick ====================

    @Test
    @DisplayName("validatePick: should throw when card is not on the board at all")
    void shouldThrowWhenCardNotOnBoard() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        when(mockBoard.findTrackPosition(p1)).thenReturn(freshOfferB(p1));
        when(mockBoard.isCardTop("cardX")).thenReturn(false);
        when(mockBoard.isCardDown("cardX")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "cardX"));
    }

    @Test
    @DisplayName("validatePick: should throw when offer does not allow picking from top row")
    void shouldThrowWhenOfferHasZeroTopPicks() {
        // offer with numUp=0: any top pick is forbidden
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        OfferCard offer = new OfferCard('D', 0, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(true);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card1"));
    }

    @Test
    @DisplayName("validatePick: should throw when offer does not allow picking from bottom row")
    void shouldThrowWhenOfferHasZeroBottomPicks() {
        // offer with numDown=0: any bottom pick is forbidden
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        OfferCard offer = new OfferCard('D', 1, 0, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(false);
        when(mockBoard.isCardDown("card1")).thenReturn(true);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card1"));
    }

    @Test
    @DisplayName("validatePick: should throw when player already picked the maximum from the top row")
    void shouldThrowWhenAlreadyPickedMaxFromTop() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");

        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(true);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card1"))).thenReturn(card1);
        phase.takeCard(mockGame, p1, "card1");

        when(mockBoard.isCardTop("card2")).thenReturn(true);
        when(mockBoard.isCardDown("card2")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card2"));
    }

    @Test
    @DisplayName("validatePick: should throw when player already picked the maximum from the bottom row")
    void shouldThrowWhenAlreadyPickedMaxFromBottom() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");

        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(false);
        when(mockBoard.isCardDown("card1")).thenReturn(true);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card1"))).thenReturn(card1);
        phase.takeCard(mockGame, p1, "card1");

        when(mockBoard.isCardTop("card2")).thenReturn(false);
        when(mockBoard.isCardDown("card2")).thenReturn(true);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card2"));
    }

    // ==================== takeCard - skip logic ====================

    @Test
    @DisplayName("takeCard skip: should set skipAvailable and return SkipDelta when no chars in upper and picks from top not exhausted")
    void shouldSkipTopWhenNoCharactersInUpperRow() {
        // offer with 1 top + 1 bottom; upper tribe has no CharacterCards
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockTribeShowed.getUpperList()).thenReturn(List.of()); // no chars

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "skip");

        assertFalse(deltas.isEmpty());
        assertInstanceOf(SkipDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("takeCard skip: should throw when chars are available in upper row and player tries to skip top pick")
    void shouldThrowWhenCharsAvailableInUpperAndPlayerSkipsTop() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);

        CharacterCard availableChar = makeCharCard("H1");
        when(mockTribeShowed.getUpperList()).thenReturn(List.of(availableChar));

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "skip"));
    }

    @Test
    @DisplayName("takeCard skip: should set skipAvailable when no chars in lower row and top picks exhausted")
    void shouldSkipBottomWhenNoCharactersInLowerRow() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");

        // offerC: 1 top + 1 bottom; first pick from top exhausts top slot
        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(true);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card1"))).thenReturn(card1);
        phase.takeCard(mockGame, p1, "card1"); // picksFromUp = 1

        // now try to skip bottom — no chars in lower
        when(mockTribeShowed.getLowerList()).thenReturn(List.of());
        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "skip");

        assertFalse(deltas.isEmpty());
        assertInstanceOf(SkipDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("takeCard skip: should throw when chars are available in lower row and player tries to skip bottom pick")
    void shouldThrowWhenCharsAvailableInLowerAndPlayerSkipsBottom() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");

        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(true);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card1"))).thenReturn(card1);
        phase.takeCard(mockGame, p1, "card1"); // picksFromUp = 1

        CharacterCard availableChar = makeCharCard("H1");
        when(mockTribeShowed.getLowerList()).thenReturn(List.of(availableChar));

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "skip"));
    }

    @Test
    @DisplayName("takeCard skip: should transition to EndTurnPhase when extra pick player sends skip")
    void shouldTransitionToEndTurnWhenExtraPickPlayerSkips() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1"); // extraPickActive = true

        // p2 sends "skip" during extra pick
        List<GameDelta> deltas = phase.takeCard(mockGame, p2, "skip");

        verify(mockGame, atLeastOnce()).setPhase(any(PlaceTotemPhase.class));
        assertFalse(deltas.isEmpty());
    }

    // ==================== takeCard - pick counter and turn progression ====================

    @Test
    @DisplayName("takeCard: should count picks from top correctly and keep turn with p1")
    void shouldCountPicksFromTopCorrectly() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");

        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockBoard.isCardTop("card1")).thenReturn(true);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card1"))).thenReturn(card);

        phase.takeCard(mockGame, p1, "card1");

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "cardX"));
    }

    @Test
    @DisplayName("takeCard: should count picks from bottom correctly and keep turn with p1")
    void shouldCountPicksFromBottomCorrectly() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickFromBottom(p1, 'C', 1, 1, 0, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "cardX"));
    }

    @Test
    @DisplayName("takeCard: should reset pick counters for next player after turn ends")
    void shouldResetPickCountersForNextPlayer() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");
        setupNormalPickB(p1, "card1", card1);
        phase.takeCard(mockGame, p1, "card1");
        setupNormalPickB(p2, "card2", card2);
        assertDoesNotThrow(() -> phase.takeCard(mockGame, p2, "card2"));
    }

    // ==================== takeCard - strategy handling ====================

    @Test
    @DisplayName("takeCard: should call registerTo and notify OnPick when card has a strategy")
    void shouldCallRegisterToAndNotifyOnPickWhenCardHasStrategy() {
        CardStrategy mockStrategy = mock(CardStrategy.class);
        CharacterCard cardWithStrategy = new CharacterCard("S1", Era.FIRST, mockStrategy, CharacterType.HUNTER, 2);

        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        setupNormalPick(p1, 'B', 1, 0, 0, "S1", cardWithStrategy);

        phase.takeCard(mockGame, p1, "S1");

        verify(mockStrategy, times(1)).registerTo(eq(mockNotificatorCenter), any(PlayerContext.class));
        verify(mockPickNotificator, times(1)).notify(any(PlayerContext.class));
    }

    @Test
    @DisplayName("takeCard: should not call registerTo or notify OnPick when card has no strategy")
    void shouldNotCallRegisterToWhenCardHasNoStrategy() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        verify(mockPickNotificator, never()).notify(any());
    }

    // ==================== takeCard - delta types ====================

    @Test
    @DisplayName("takeCard: should return CharacterCardPickedDelta for a CharacterCard")
    void shouldReturnCharacterCardPickedDeltaForCharacterCard() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertInstanceOf(CharacterCardPickedDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("takeCard: should return BuildingCardPickedDelta for a BuildingCard")
    void shouldReturnBuildingCardPickedDeltaForBuildingCard() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        BuildingCard building = makeBuildingCard("bld1");
        setupNormalPick(p1, 'B', 1, 0, 0, "bld1", building);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "bld1");

        assertInstanceOf(BuildingCardPickedDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("takeCard: should return SkipDelta when card is null (skip path in buildCardDelta)")
    void shouldReturnSkipDeltaWhenCardIsNull() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        OfferCard offer = new OfferCard('C', 1, 1, 0, 3);
        offer.placeTotem(p1);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offer);
        when(mockTribeShowed.getUpperList()).thenReturn(List.of());

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "skip");

        assertInstanceOf(SkipDelta.class, deltas.getFirst());
    }

    @Test
    @DisplayName("takeCard: should return at least one delta")
    void shouldReturnNonEmptyDeltaAfterTakeCard() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertFalse(deltas.isEmpty());
    }

    // ==================== takeCard - end turn transition ====================

    @Test
    @DisplayName("takeCard: should transition to PlaceTotemPhase after all players have picked")
    void shouldTransitionToPlaceTotemPhaseAfterAllPlayersPick() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).setPhase(any(PlaceTotemPhase.class));
    }

    @Test
    @DisplayName("takeCard: should include EndTurnDelta in returned deltas after transition")
    void shouldReturnEndTurnDeltaAfterTransition() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        setupEndTurnMocks();

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertTrue(deltas.size() >= 2);
    }

    @Test
    @DisplayName("takeCard: should resolve all events during EndTurnPhase")
    void shouldResolveAllEventsOnEndTurn() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        for (EventType e : EventType.values()) {
            verify(mockEventNotificator).notify(eq(e), any(PlayerContext.class));
        }
    }

    @Test
    @DisplayName("takeCard: should transition to EndGamePhase after turn 10")
    void shouldTransitionToEndGameAfterTurn10() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        when(mockGame.getCurrentTurn()).thenReturn(11);

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).setPhase(any(EndGamePhase.class));
    }

    @Test
    @DisplayName("takeCard: should increment turn counter during EndTurnPhase")
    void shouldIncrementTurnOnEndTurn() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).incrementTurn();
    }

    @Test
    @DisplayName("takeCard: should return three deltas in correct order on last card of last turn")
    void shouldReturnThreeDeltasInCorrectOrderOnLastCardOfLastTurn() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);
        when(mockGame.getCurrentTurn()).thenReturn(11);
        when(mockBoard.getPlaceOrder()).thenReturn(List.of(p1, p2, p3));

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertEquals(3, deltas.size());
        assertInstanceOf(CharacterCardPickedDelta.class, deltas.get(0));
        assertInstanceOf(EndTurnDelta.class, deltas.get(1));
        assertInstanceOf(EndGameDelta.class, deltas.get(2));
    }

    @Test
    @DisplayName("takeCard: should return three deltas with BuildingCardPickedDelta first when building card picked on last turn")
    void shouldReturnThreeDeltasWithBuildingDeltaFirstWhenBuildingCardPicked() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        BuildingCard building = makeBuildingCard("b1");
        setupNormalPick(p1, 'B', 1, 0, 0, "b1", building);
        when(mockGame.getCurrentTurn()).thenReturn(11);
        when(mockBoard.getPlaceOrder()).thenReturn(List.of(p1, p2, p3));

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "b1");

        assertEquals(3, deltas.size());
        assertInstanceOf(BuildingCardPickedDelta.class, deltas.get(0));
        assertInstanceOf(EndTurnDelta.class, deltas.get(1));
        assertInstanceOf(EndGameDelta.class, deltas.get(2));
    }

    // ==================== takeCard - extra pick ====================

    @Test
    @DisplayName("takeCard: should activate extra pick when a player deserves it")
    void shouldActivateExtraPickIfPlayerDeservesIt() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p3, "cardX"));
    }

    @Test
    @DisplayName("takeCard: should throw when wrong player picks during extra pick")
    void shouldThrowIfWrongPlayerPicksDuringExtraPick() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p3, "cardX"));
    }

    @Test
    @DisplayName("takeCard: should throw when extra pick player tries to pick from bottom row")
    void shouldThrowIfExtraPickPlayerPicksFromBottom() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        when(mockBoard.isCardTop("card2")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "card2"));
    }

    @Test
    @DisplayName("takeCard: should transition to EndTurnPhase after extra pick is completed")
    void shouldTransitionToEndTurnAfterExtraPick() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");
        setupNormalPickB(p1, "card1", card1);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        when(mockBoard.isCardTop("card2")).thenReturn(true);
        when(mockBoard.takeCard(any(PlayerContext.class), eq("card2"))).thenReturn(card2);
        when(mockBoard.findTrackPosition(p2)).thenReturn(freshOfferB(p2));

        phase.takeCard(mockGame, p2, "card2");

        verify(mockGame, atLeastOnce()).setPhase(any(PlaceTotemPhase.class));
    }

    @Test
    @DisplayName("takeCard: should remain in PlayerOfferPhase after all pick when extra pick is active")
    void shouldRemainInPlayerOfferPhaseWhenExtraPickActive() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        // phase should NOT have transitioned — game.setPhase should not have been called with EndTurnPhase
        verify(mockGame, never()).setPhase(any(EndTurnPhase.class));
        assertFalse(deltas.isEmpty());
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a PlayerOfferPhaseSnapshot with correct nicknames and index")
    void shouldReturnCorrectSnapshot() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        PlayerOfferPhaseSnapshot snapshot = (PlayerOfferPhaseSnapshot) phase.toSnapshot();

        assertNotNull(snapshot);
        assertEquals(List.of("alice", "bob", "charlie"), snapshot.getActionOrderNicknames());
        assertEquals(0, snapshot.getCurrIdx());
    }

    @Test
    @DisplayName("toSnapshot: should reflect updated currIdx after a player completes their turn")
    void shouldReflectUpdatedCurrIdxInSnapshot() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        PlayerOfferPhaseSnapshot snapshot = (PlayerOfferPhaseSnapshot) phase.toSnapshot();
        assertEquals(1, snapshot.getCurrIdx());
    }

    @Test
    @DisplayName("toSnapshot: should include extraPickPlayer nickname when extra pick is active")
    void shouldIncludeExtraPickPlayerNicknameInSnapshot() {
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPickB(p1, "card1", card);

        phase.takeCard(mockGame, p1, "card1"); // activates extra pick for p2

        PlayerOfferPhaseSnapshot snapshot = (PlayerOfferPhaseSnapshot) phase.toSnapshot();
        assertEquals("bob", snapshot.getExtraPickPlayerNickname());
        assertTrue(snapshot.getExtraPickActive());
    }

    @Test
    @DisplayName("toSnapshot: should have null extraPickPlayerNickname when no extra pick is active")
    void shouldHaveNullExtraPickPlayerNicknameWhenNoExtraPick() {
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        PlayerOfferPhaseSnapshot snapshot = (PlayerOfferPhaseSnapshot) phase.toSnapshot();
        assertNull(snapshot.getExtraPickPlayerNickname());
        assertFalse(snapshot.getExtraPickActive());
    }
}