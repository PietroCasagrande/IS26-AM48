package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.*;
import it.polimi.ingsw.am48.model.snapshot.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientModelTest {

    private ClientModel model;
    private GameSnapshot mockSnapshot;

    @BeforeEach
    void setUp() {
        model = new ClientModel();
        mockSnapshot = buildMockSnapshot("game1", 1, "PlaceTotemPhase",
                List.of("c1", "c2"), List.of("c3"),
                List.of("b1"), List.of(),
                Map.of('A', "alice"), List.of("alice", "bob"),
                List.of(
                        buildMockPlayerSnapshot("alice", 3, 5, List.of("c1"), List.of()),
                        buildMockPlayerSnapshot("bob", 0, 0, List.of(), List.of())
                )
        );
    }

    // --- HELPER ---

    private GameSnapshot buildMockSnapshot(
            String gameId, int turn, String phase,
            List<String> upperTribe, List<String> lowerTribe,
            List<String> upperBuilding, List<String> lowerBuilding,
            Map<Character, String> offerTrack, List<String> turnCardOrder,
            List<PlayerSnapshot> players) {

        GameSnapshot snap = mock(GameSnapshot.class);
        PhaseSnapshot phaseSnap = mock(PhaseSnapshot.class);
        BoardSnapshot boardSnap = mock(BoardSnapshot.class);
        OfferTrackSnapshot trackSnap = mock(OfferTrackSnapshot.class);
        OfferTurnCardSnapshot turnCardSnap = mock(OfferTurnCardSnapshot.class);

        when(snap.getGameId()).thenReturn(gameId);
        when(snap.getCurrentTurn()).thenReturn(turn);
        when(snap.getPhase()).thenReturn(phaseSnap);
        when(phaseSnap.getPhaseName()).thenReturn(phase);
        when(snap.getBoard()).thenReturn(boardSnap);
        when(boardSnap.getUpperRowCardIds()).thenReturn(upperTribe);
        when(boardSnap.getLowerRowCardIds()).thenReturn(lowerTribe);
        when(boardSnap.getBuildingUpperRowCardIds()).thenReturn(upperBuilding);
        when(boardSnap.getBuildingLowerRowCardIds()).thenReturn(lowerBuilding);
        when(boardSnap.getOfferTrack()).thenReturn(trackSnap);
        when(trackSnap.getTotemPositions()).thenReturn(offerTrack);
        when(boardSnap.getOfferTurnCard()).thenReturn(turnCardSnap);
        when(turnCardSnap.getTotemOrder()).thenReturn(turnCardOrder);
        when(snap.getPlayers()).thenReturn(players);

        return snap;
    }

    private PlayerSnapshot buildMockPlayerSnapshot(
            String nickname, int food, int points,
            List<String> characters, List<String> buildings) {

        PlayerSnapshot snap = mock(PlayerSnapshot.class);
        TribeSnapshot tribeSnap = mock(TribeSnapshot.class);

        when(snap.getNickname()).thenReturn(nickname);
        when(snap.getTotemColor()).thenReturn("RED");
        when(snap.getTribe()).thenReturn(tribeSnap);
        when(tribeSnap.getCurrentFood()).thenReturn(food);
        when(tribeSnap.getCurrentPrestigePoints()).thenReturn(points);
        when(tribeSnap.getCharacterCardIds()).thenReturn(characters);
        when(tribeSnap.getBuildingCardIds()).thenReturn(buildings);

        return snap;
    }

    // --- setInitialState ---

    @Test
    void shouldInitializeStateFromSnapshot() {
        // lo stato viene costruito correttamente dallo snapshot
        model.setInitialState(mockSnapshot);
        assertNotNull(model.getState());
    }

    @Test
    void shouldSetGameIdFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals("game1", model.getState().getGameId());
    }

    @Test
    void shouldSetCurrentTurnFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(1, model.getState().getCurrentTurn());
    }

    @Test
    void shouldSetCurrentPhaseFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals("PlaceTotemPhase", model.getState().getCurrentPhase());
    }

    @Test
    void shouldSetUpperTribeRowFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(List.of("c1", "c2"), model.getState().getUpperRowCardIds());
    }

    @Test
    void shouldSetLowerTribeRowFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(List.of("c3"), model.getState().getLowerRowCardIds());
    }

    @Test
    void shouldSetBuildingUpperRowFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(List.of("b1"), model.getState().getBuildingUpperIds());
    }

    @Test
    void shouldSetOfferTrackFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals("alice", model.getState().getOfferTrackPositions().get('A'));
    }

    @Test
    void shouldSetOfferTurnCardOrderFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(List.of("alice", "bob"), model.getState().getOfferTurnCardOrder());
    }

    @Test
    void shouldSetPlayersFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertNotNull(model.getState().getPlayer("alice"));
        assertNotNull(model.getState().getPlayer("bob"));
    }

    @Test
    void shouldSetPlayerFoodFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(3, model.getState().getPlayer("alice").getFood());
    }

    @Test
    void shouldSetPlayerPointsFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertEquals(5, model.getState().getPlayer("alice").getPoints());
    }

    @Test
    void shouldSetPlayerCharacterCardsFromSnapshot() {
        model.setInitialState(mockSnapshot);
        assertTrue(model.getState().getPlayer("alice").getCharacterCardIds().contains("c1"));
    }

    // --- applyDelta ---

    @Test
    void shouldCallApplyToOnDelta() {
        // applyDelta delega al metodo applyTo del delta
        model.setInitialState(mockSnapshot);
        GameDelta mockDelta = mock(GameDelta.class);
        model.applyDelta(mockDelta);
        verify(mockDelta, times(1)).applyTo(model);
    }

    // --- updateTribeShowed ---

    @Test
    void shouldUpdateUpperTribeRow() {
        model.setInitialState(mockSnapshot);
        model.updateTribeShowed(List.of("newC1", "newC2"), List.of("newC3"));
        assertEquals(List.of("newC1", "newC2"), model.getState().getUpperRowCardIds());
    }

    @Test
    void shouldUpdateLowerTribeRow() {
        model.setInitialState(mockSnapshot);
        model.updateTribeShowed(List.of("newC1"), List.of("newC3", "newC4"));
        assertEquals(List.of("newC3", "newC4"), model.getState().getLowerRowCardIds());
    }

    // --- updateBuildingShowed ---

    @Test
    void shouldUpdateUpperBuildingRow() {
        model.setInitialState(mockSnapshot);
        model.updateBuildingShowed(List.of("newB1"), List.of());
        assertEquals(List.of("newB1"), model.getState().getBuildingUpperIds());
    }

    @Test
    void shouldUpdateLowerBuildingRow() {
        model.setInitialState(mockSnapshot);
        model.updateBuildingShowed(List.of(), List.of("newB2"));
        assertEquals(List.of("newB2"), model.getState().getBuildingLowerIds());
    }

    // --- updatePlayerFood ---

    @Test
    void shouldUpdateFoodForCorrectPlayer() {
        model.setInitialState(mockSnapshot);
        model.updatePlayerFood("alice", 10);
        assertEquals(10, model.getState().getPlayer("alice").getFood());
    }

    @Test
    void shouldNotUpdateFoodForOtherPlayers() {
        model.setInitialState(mockSnapshot);
        model.updatePlayerFood("alice", 10);
        assertEquals(0, model.getState().getPlayer("bob").getFood());
    }

    // --- updatePlayerPoints ---

    @Test
    void shouldUpdatePointsForCorrectPlayer() {
        model.setInitialState(mockSnapshot);
        model.updatePlayerPoints("alice", 15);
        assertEquals(15, model.getState().getPlayer("alice").getPoints());
    }

    @Test
    void shouldNotUpdatePointsForOtherPlayers() {
        model.setInitialState(mockSnapshot);
        model.updatePlayerPoints("alice", 15);
        assertEquals(0, model.getState().getPlayer("bob").getPoints());
    }

    // --- addCardToPlayerTribe ---

    @Test
    void shouldAddCharacterCardToCorrectPlayer() {
        model.setInitialState(mockSnapshot);
        model.addCardToPlayerTribe("bob", "newCard");
        assertTrue(model.getState().getPlayer("bob").getCharacterCardIds().contains("newCard"));
    }

    @Test
    void shouldNotAddCharacterCardToOtherPlayers() {
        model.setInitialState(mockSnapshot);
        model.addCardToPlayerTribe("bob", "newCard");
        assertFalse(model.getState().getPlayer("alice").getCharacterCardIds().contains("newCard"));
    }

    // --- addBuildingToPlayer ---

    @Test
    void shouldAddBuildingCardToCorrectPlayer() {
        model.setInitialState(mockSnapshot);
        model.addBuildingToPlayer("alice", "building1");
        assertTrue(model.getState().getPlayer("alice").getBuildingCardIds().contains("building1"));
    }

    @Test
    void shouldNotAddBuildingCardToOtherPlayers() {
        model.setInitialState(mockSnapshot);
        model.addBuildingToPlayer("alice", "building1");
        assertFalse(model.getState().getPlayer("bob").getBuildingCardIds().contains("building1"));
    }

    // --- placeTotemOnTrack ---

    @Test
    void shouldPlaceTotemOnCorrectTrackPosition() {
        model.setInitialState(mockSnapshot);
        model.placeTotemOnTrack('B', "bob");
        assertEquals("bob", model.getState().getOfferTrackPositions().get('B'));
    }

    @Test
    void shouldRemoveTotemFromTurnCardWhenPlacedOnTrack() {
        // quando il totem viene piazzato sulla track sparisce dalla turn card
        model.setInitialState(mockSnapshot);
        model.placeTotemOnTrack('B', "bob");
        assertFalse(model.getState().getOfferTurnCardOrder().contains("bob"));
    }

    @Test
    void shouldNotAffectOtherTrackPositionsWhenPlacing() {
        model.setInitialState(mockSnapshot);
        model.placeTotemOnTrack('B', "bob");
        assertEquals("alice", model.getState().getOfferTrackPositions().get('A'));
    }

    // --- returnTotemToTurnCard ---

    @Test
    void shouldAddPlayerToTurnCardOrder() {
        model.setInitialState(mockSnapshot);
        // alice è sulla tessera A, la rimuoviamo dalla track e la aggiungiamo alla turn card
        model.returnTotemToTurnCard("alice");
        assertTrue(model.getState().getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldRemovePlayerFromTrackWhenReturning() {
        model.setInitialState(mockSnapshot);
        model.returnTotemToTurnCard("alice");
        assertFalse(model.getState().getOfferTrackPositions().containsValue("alice"));
    }

    // --- updateOfferTurnCardOrder ---

    @Test
    void shouldReplaceEntireTurnCardOrder() {
        model.setInitialState(mockSnapshot);
        model.updateOfferTurnCardOrder(List.of("bob", "alice"));
        assertEquals(List.of("bob", "alice"), model.getState().getOfferTurnCardOrder());
    }

    // --- setPhase ---

    @Test
    void shouldUpdateCurrentPhase() {
        model.setInitialState(mockSnapshot);
        model.setPhase("PlayerOfferPhase");
        assertEquals("PlayerOfferPhase", model.getState().getCurrentPhase());
    }

    // --- incrementTurn ---

    @Test
    void shouldUpdateCurrentTurn() {
        model.setInitialState(mockSnapshot);
        model.incrementTurn(2);
        assertEquals(2, model.getState().getCurrentTurn());
    }

    // --- observer ---

    @Test
    void shouldNotifyObserverOnSetInitialState() {
        ModelObserver mockObserver = mock(ModelObserver.class);
        model.registerObserver(mockObserver);
        model.setInitialState(mockSnapshot);
        verify(mockObserver, times(1)).onStateUpdated(any());
    }

    @Test
    void shouldNotifyObserverOnApplyDelta() {
        ModelObserver mockObserver = mock(ModelObserver.class);
        model.registerObserver(mockObserver);
        model.setInitialState(mockSnapshot);
        model.applyDelta(mock(GameDelta.class));
        verify(mockObserver, times(2)).onStateUpdated(any());
    }

    // --- FINE TURNO: 2 delta nell'ordine corretto ---

    @Test
    void shouldApplyCharacterPickedDeltaThenEndTurnDeltaInCorrectOrder() {
        // verifica che applicando i 2 delta nell'ordine corretto lo stato finale sia coerente
        model.setInitialState(mockSnapshot);

        // delta 1: il player pesca una carta personaggio, il totem ritorna
        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "newCard",
                List.of("c10", "c11"), List.of("c12"),
                6, 5, true // food aggiornato=6, points=5, totemReturned=true
        );

        // delta 2: fine turno con cibo e punti aggiornati per tutti
        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),   // food dopo sustenance
                Map.of("alice", 5, "bob", 3),   // prestige aggiornato
                List.of("c20", "c21"), List.of("c22"),
                List.of("b10"), List.of()
        );

        // applica nell'ordine corretto
        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);

        // verifica che il pick sia stato applicato
        assertTrue(model.getState().getPlayer("alice").getCharacterCardIds().contains("newCard"));

        // verifica che endturn abbia sovrascritto food e points
        assertEquals(4, model.getState().getPlayer("alice").getFood());
        assertEquals(5, model.getState().getPlayer("alice").getPoints());
        assertEquals(2, model.getState().getPlayer("bob").getFood());
        assertEquals(3, model.getState().getPlayer("bob").getPoints());

        // verifica showed aggiornate da endturn
        assertEquals(List.of("c20", "c21"), model.getState().getUpperRowCardIds());
        assertEquals(List.of("c22"), model.getState().getLowerRowCardIds());
        assertEquals(List.of("b10"), model.getState().getBuildingUpperIds());

        // verifica totem tornato sulla turn card
        assertTrue(model.getState().getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldApplyBuildingPickedDeltaThenEndTurnDeltaInCorrectOrder() {
        // stesso scenario ma con BuildingCard
        model.setInitialState(mockSnapshot);

        BuildingCardPickedDelta pickDelta = new BuildingCardPickedDelta(
                "alice", "newBuilding",
                List.of("b10"), List.of("b11"),
                1, 5, true // food=1 dopo pagamento edificio, points=5, totemReturned
        );

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 0, "bob", 2),
                Map.of("alice", 5, "bob", 0),
                List.of("c20"), List.of("c21"),
                List.of("b10"), List.of()
        );

        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);

        // verifica building aggiunto
        assertTrue(model.getState().getPlayer("alice").getBuildingCardIds().contains("newBuilding"));

        // verifica endturn ha sovrascritto i valori
        assertEquals(0, model.getState().getPlayer("alice").getFood());
        assertEquals(5, model.getState().getPlayer("alice").getPoints());

        // verifica totem tornato
        assertTrue(model.getState().getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldNotAffectOtherPlayersPickWhenApplyingPickDelta() {
        // il pick di alice non deve cambiare lo stato di bob
        model.setInitialState(mockSnapshot);

        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "newCard",
                List.of("c10"), List.of("c11"),
                6, 5, false
        );

        model.applyDelta(pickDelta);

        assertFalse(model.getState().getPlayer("bob").getCharacterCardIds().contains("newCard"));
        assertEquals(0, model.getState().getPlayer("bob").getFood());
    }

// --- FINE PARTITA: 3 delta nell'ordine corretto ---

    @Test
    void shouldApplyAllThreeDeltasInCorrectOrderOnGameEnd() {
        // verifica il flusso completo fine partita: pick → endturn → endgame
        model.setInitialState(mockSnapshot);

        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "lastCard",
                List.of(), List.of(),
                6, 5, true
        );

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7),
                List.of(), List.of(),
                List.of(), List.of()
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18),
                "alice"
        );

        // applica nell'ordine corretto
        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);
        model.applyDelta(endGameDelta);

        // verifica carta aggiunta da pickDelta
        assertTrue(model.getState().getPlayer("alice").getCharacterCardIds().contains("lastCard"));

        // verifica che endGameDelta abbia sovrascritto i punti finali
        assertEquals(25, model.getState().getPlayer("alice").getPoints());
        assertEquals(18, model.getState().getPlayer("bob").getPoints());
    }

    @Test
    void shouldOverwriteEndTurnPointsWithEndGamePoints() {
        // endgame sovrascrive i punti di endturn con i punteggi finali
        model.setInitialState(mockSnapshot);

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7), // punti dopo eventi
                List.of(), List.of(), List.of(), List.of()
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18), // punti finali dopo calcolo endgame
                "alice"
        );

        model.applyDelta(endTurnDelta);
        model.applyDelta(endGameDelta);

        // i punti finali devono essere quelli di endgame, non di endturn
        assertEquals(25, model.getState().getPlayer("alice").getPoints());
        assertEquals(18, model.getState().getPlayer("bob").getPoints());
    }

    @Test
    void shouldApplyEndGameDeltaToAllPlayers() {
        // endgame aggiorna i punti di tutti i player, non solo del vincitore
        model.setInitialState(mockSnapshot);

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18),
                "alice"
        );

        model.applyDelta(endGameDelta);

        assertEquals(25, model.getState().getPlayer("alice").getPoints());
        assertEquals(18, model.getState().getPlayer("bob").getPoints());
    }

    @Test
    void shouldReverseDeltaOrderResultInIncorrectState() {
        // se i delta vengono applicati nell'ordine sbagliato lo stato finale è diverso
        model.setInitialState(mockSnapshot);

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7),
                List.of("c20"), List.of(), List.of(), List.of()
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18),
                "alice"
        );

        // ordine invertito: endgame prima, endturn dopo
        model.applyDelta(endGameDelta);
        model.applyDelta(endTurnDelta);

        // endturn sovrascrive endgame → stato sbagliato
        assertEquals(10, model.getState().getPlayer("alice").getPoints()); // endturn ha sovrascritto
        assertNotEquals(25, model.getState().getPlayer("alice").getPoints());
    }
}