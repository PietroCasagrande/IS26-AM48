package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.notificator.*;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private OfferCard offerB; // tessera B: 1 pick dall'alto, 0 dal basso, 0 cibo bonus
    private OfferCard offerA; // tessera A: 1 pick dall'alto, 0 dal basso, 3 cibo bonus

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

        offerB = new OfferCard('B', 1, 0, 0, 3);
        offerA = new OfferCard('A', 1, 0, 3, 3);
    }

    // --- HELPER ---

    private CharacterCard makeCharCard(String id) {
        return new CharacterCard(id, Era.FIRST, null, CharacterType.HUNTER, 2);
    }

    private void setupNormalPick(Player player, OfferCard offer, String cardId, Card card) {
        when(mockBoard.findTrackPosition(player)).thenReturn(offer);
        when(mockBoard.isCardTop(cardId)).thenReturn(true);
        when(mockBoard.isCardDown(cardId)).thenReturn(false);
        when(mockBoard.takeCard(player, cardId)).thenReturn(card);
    }

    private void setupEndTurnMocks() {
        when(mockGame.getCurrentTurn()).thenReturn(1);
        when(mockBoard.getPlaceOrder()).thenReturn(List.of(p1, p2, p3));
    }

    // --- COSTRUTTORE ---

    @Test
    void shouldSkipFirstPlayerAndGiveFoodIfOnCardA() {
        // se il primo player è sulla tessera A riceve cibo e viene skippato
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerA);
        new PlayerOfferPhase(List.of(p1, p2, p3));
        assertEquals(3, p1.getFood());
        verify(mockBoard).returnTotem(p1);
    }

    @Test
    void shouldNotSkipFirstPlayerIfNotOnCardA() {
        // se il primo player non è sulla tessera A non viene skippato
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        new PlayerOfferPhase(List.of(p1, p2, p3));
        assertEquals(0, p1.getFood());
        verify(mockBoard, never()).returnTotem(p1);
    }

    // --- VALIDAZIONE TURNO ---

    @Test
    void shouldThrowIfWrongPlayerTakesCard() {
        // player non di turno non può pescare
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "card1"));
    }

    @Test
    void shouldNotThrowIfCorrectPlayerTakesCard() {
        // il player di turno può pescare
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        assertDoesNotThrow(() -> phase.takeCard(mockGame, p1, "card1"));
    }

    // --- VALIDAZIONE PICK ---

    @Test
    void shouldThrowIfCardNotOnBoard() {
        // carta non presente sul tabellone
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        when(mockBoard.isCardTop("card1")).thenReturn(false);
        when(mockBoard.isCardDown("card1")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card1"));
    }

    @Test
    void shouldThrowIfExceedsPicksFromTop() {
        // supera il numero di pick consentiti dalla fila superiore
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");
        setupNormalPick(p1, offerB, "card1", card1);
        phase.takeCard(mockGame, p1, "card1"); // prima pesca ok
        when(mockBoard.isCardTop("card2")).thenReturn(true);
        when(mockBoard.isCardDown("card2")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card2")); // supera limite
    }

    @Test
    void shouldThrowIfExceedsPicksFromBottom() {
        // supera il numero di pick consentiti dalla fila inferiore
        OfferCard offerOneDown = new OfferCard('B', 0, 1, 0, 3);
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerOneDown);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerOneDown);
        when(mockBoard.isCardTop("card1")).thenReturn(false);
        when(mockBoard.isCardDown("card1")).thenReturn(true);
        when(mockBoard.takeCard(p1, "card1")).thenReturn(card1);
        phase.takeCard(mockGame, p1, "card1"); // prima pesca ok
        when(mockBoard.isCardTop("card2")).thenReturn(false);
        when(mockBoard.isCardDown("card2")).thenReturn(true);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "card2")); // supera limite
    }

    // --- AVANZAMENTO TURNO ---

    @Test
    void shouldAdvanceToNextPlayerAfterAllPicksDone() {
        // dopo che p1 ha finito i pick tocca a p2
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        phase.takeCard(mockGame, p1, "card1");
        // ora tocca a p2, p1 non può più pescare
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p1, "cardX"));
    }

    @Test
    void shouldResetPickCountersForNextPlayer() {
        // i contatori di pescate vengono resettati per il prossimo player
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");
        setupNormalPick(p1, offerB, "card1", card1);
        phase.takeCard(mockGame, p1, "card1"); // p1 finisce
        setupNormalPick(p2, offerB, "card2", card2);
        // p2 può pescare dalla fila superiore senza problemi
        assertDoesNotThrow(() -> phase.takeCard(mockGame, p2, "card2"));
    }

    // --- TRANSIZIONE A ENDTURNPHASE ---

    @Test
    void shouldTransitionToEndTurnPhaseAfterAllPlayersPick() {
        // dopo che tutti i player hanno pescato si transisce a EndTurnPhase
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).setPhase(any(PlaceTotemPhase.class));
    }

    @Test
    void shouldReturnEndTurnDeltaAfterTransition() {
        // dopo la transizione il delta include anche EndTurnDelta
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        setupEndTurnMocks();

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertTrue(deltas.size() >= 2); // CardPickedDelta + EndTurnDelta
    }

    @Test
    void shouldResolveAllEventsOnEndTurn() {
        // tutti gli eventi vengono risolti durante EndTurnPhase
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        for (EventType e : EventType.values()) {
            verify(mockEventNotificator).notify(eq(e), any(PlayerContext.class));
        }
    }

    @Test
    void shouldTransitionToEndGameAfterTurn10() {
        // dopo il turno 10 si transisce a EndGamePhase
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        when(mockGame.getCurrentTurn()).thenReturn(11); // turno 11 → fine partita

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).setPhase(any(EndGamePhase.class));
    }

    @Test
    void shouldIncrementTurnOnEndTurn() {
        // il turno viene incrementato durante EndTurnPhase
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1");

        verify(mockGame).incrementTurn();
    }

    // --- EXTRA PICK ---

    @Test
    void shouldActivateExtraPickIfPlayerDeservesIt() {
        // se un player ha il diritto all'extra pick extraPickActive viene settato
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);

        phase.takeCard(mockGame, p1, "card1");

        // p3 non ha l'edificio, non può pescare durante extra pick
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p3, "cardX"));
    }

    @Test
    void shouldThrowIfWrongPlayerPicksDuringExtraPick() {
        // durante l'extra pick solo il player con l'edificio può pescare
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);

        phase.takeCard(mockGame, p1, "card1"); // finisce round, extraPickActive = true

        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p3, "cardX")); // p3 non ha l'edificio
    }

    @Test
    void shouldThrowIfExtraPickPlayerPicksFromBottom() {
        // durante l'extra pick si può prendere solo dalla fila superiore
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);

        phase.takeCard(mockGame, p1, "card1"); // extraPickActive = true

        when(mockBoard.isCardTop("card2")).thenReturn(false);
        assertThrows(InvalidActionException.class,
                () -> phase.takeCard(mockGame, p2, "card2"));
    }

    @Test
    void shouldTransitionToEndTurnAfterExtraPick() {
        // dopo l'extra pick si transisce a EndTurnPhase
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        p2.setExtraPickRight();
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1));
        Card card1 = makeCharCard("card1");
        Card card2 = makeCharCard("card2");
        setupNormalPick(p1, offerB, "card1", card1);
        setupEndTurnMocks();

        phase.takeCard(mockGame, p1, "card1"); // finisce round, extraPickActive = true

        when(mockBoard.isCardTop("card2")).thenReturn(true);
        when(mockBoard.takeCard(p2, "card2")).thenReturn(card2);

        phase.takeCard(mockGame, p2, "card2"); // extra pick completato

        verify(mockGame, atLeastOnce()).setPhase(any(PlaceTotemPhase.class));
    }

    // --- DELTA ---

    @Test
    void shouldReturnNonEmptyDeltaAfterTakeCard() {
        // takeCard restituisce sempre almeno un delta
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertFalse(deltas.isEmpty());
    }

    @Test
    void shouldReturnCharacterCardPickedDeltaForCharacterCard() {
        // per una CharacterCard il delta è di tipo CharacterCardPickedDelta
        when(mockBoard.findTrackPosition(p1)).thenReturn(offerB);
        PlayerOfferPhase phase = new PlayerOfferPhase(List.of(p1, p2, p3));
        Card card = makeCharCard("card1");
        setupNormalPick(p1, offerB, "card1", card);

        List<GameDelta> deltas = phase.takeCard(mockGame, p1, "card1");

        assertInstanceOf(it.polimi.ingsw.am48.model.delta.CharacterCardPickedDelta.class, deltas.getFirst());
    }
}