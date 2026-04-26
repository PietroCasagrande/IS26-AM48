package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientGameStateTest {

    private GameSnapshot mockSnapshot;

    @BeforeEach
    void setUp() {
        mockSnapshot = buildMockSnapshot(
                "game1", 1, "PlaceTotemPhase",
                List.of("c1", "c2"), List.of("c3"),
                List.of("b1"), List.of(),
                Map.of('A', "alice"), List.of("alice", "bob"),
                List.of(
                        buildMockPlayerSnapshot("alice", 3, 5,
                                List.of("c1"), List.of("b1")),
                        buildMockPlayerSnapshot("bob", 0, 0,
                                List.of(), List.of())
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

    // --- fromSnapshot ---

    @Test
    void shouldSetGameIdFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals("game1", state.getGameId());
    }

    @Test
    void shouldSetCurrentTurnFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals(1, state.getCurrentTurn());
    }

    @Test
    void shouldSetCurrentPhaseFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals("PlaceTotemPhase", state.getCurrentPhase());
    }

    @Test
    void shouldSetUpperTribeRowFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("c1", "c2"), state.getUpperRowCardIds());
    }

    @Test
    void shouldSetLowerTribeRowFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("c3"), state.getLowerRowCardIds());
    }

    @Test
    void shouldSetBuildingUpperRowFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("b1"), state.getBuildingUpperIds());
    }

    @Test
    void shouldSetBuildingLowerRowFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertTrue(state.getBuildingLowerIds().isEmpty());
    }

    @Test
    void shouldSetOfferTrackPositionsFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals("alice", state.getOfferTrackPositions().get('A'));
    }

    @Test
    void shouldSetOfferTurnCardOrderFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertEquals(List.of("alice", "bob"), state.getOfferTurnCardOrder());
    }

    @Test
    void shouldSetPlayersFromSnapshot() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertNotNull(state.getPlayer("alice"));
        assertNotNull(state.getPlayer("bob"));
    }

    @Test
    void shouldReturnNullForUnknownPlayer() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertNull(state.getPlayer("unknown"));
    }

    // --- updateTribeShowed ---

    @Test
    void shouldUpdateUpperTribeRow() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updateTribeShowed(List.of("newC1"), List.of("newC2"));
        assertEquals(List.of("newC1"), state.getUpperRowCardIds());
    }

    @Test
    void shouldUpdateLowerTribeRow() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updateTribeShowed(List.of("newC1"), List.of("newC2"));
        assertEquals(List.of("newC2"), state.getLowerRowCardIds());
    }

    @Test
    void shouldNotModifyOriginalListOnUpdate() {
        // updateTribeShowed fa una copia difensiva
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        List<String> upper = new ArrayList<>(List.of("c1"));
        state.updateTribeShowed(upper, List.of());
        upper.add("c2"); // modifica la lista originale
        assertEquals(List.of("c1"), state.getUpperRowCardIds()); // lo stato non deve cambiare
    }

    // --- updateBuildingShowed ---

    @Test
    void shouldUpdateUpperBuildingRow() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updateBuildingShowed(List.of("newB1"), List.of());
        assertEquals(List.of("newB1"), state.getBuildingUpperIds());
    }

    @Test
    void shouldUpdateLowerBuildingRow() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updateBuildingShowed(List.of(), List.of("newB2"));
        assertEquals(List.of("newB2"), state.getBuildingLowerIds());
    }

    // --- updatePlayerFood ---

    @Test
    void shouldUpdateFoodForCorrectPlayer() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updatePlayerFood("alice", 10);
        assertEquals(10, state.getPlayer("alice").getFood());
    }

    @Test
    void shouldNotAffectOtherPlayersOnFoodUpdate() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updatePlayerFood("alice", 10);
        assertEquals(0, state.getPlayer("bob").getFood());
    }

    // --- updatePlayerPoints ---

    @Test
    void shouldUpdatePointsForCorrectPlayer() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updatePlayerPoints("alice", 15);
        assertEquals(15, state.getPlayer("alice").getPoints());
    }

    @Test
    void shouldNotAffectOtherPlayersOnPointsUpdate() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updatePlayerPoints("alice", 15);
        assertEquals(0, state.getPlayer("bob").getPoints());
    }

    // --- addCharacterToPlayer ---

    @Test
    void shouldAddCharacterCardToCorrectPlayer() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.addCharacterToPlayer("bob", "newCard");
        assertTrue(state.getPlayer("bob").getCharacterCardIds().contains("newCard"));
    }

    @Test
    void shouldNotAddCharacterCardToOtherPlayers() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.addCharacterToPlayer("bob", "newCard");
        assertFalse(state.getPlayer("alice").getCharacterCardIds().contains("newCard"));
    }

    // --- addBuildingToPlayer ---

    @Test
    void shouldAddBuildingCardToCorrectPlayer() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.addBuildingToPlayer("bob", "newBuilding");
        assertTrue(state.getPlayer("bob").getBuildingCardIds().contains("newBuilding"));
    }

    // --- placeTotemOnTrack ---

    @Test
    void shouldPlaceTotemOnCorrectPosition() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.placeTotemOnTrack('B', "bob");
        assertEquals("bob", state.getOfferTrackPositions().get('B'));
    }

    @Test
    void shouldNotAffectOtherTrackPositions() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.placeTotemOnTrack('B', "bob");
        assertEquals("alice", state.getOfferTrackPositions().get('A'));
    }

    // --- removeTotemFromTurnCard ---

    @Test
    void shouldRemovePlayerFromTurnCard() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.removeTotemFromTurnCard("alice");
        assertFalse(state.getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldNotRemoveOtherPlayersFromTurnCard() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.removeTotemFromTurnCard("alice");
        assertTrue(state.getOfferTurnCardOrder().contains("bob"));
    }

    // --- returnTotemToTurnCard ---

    @Test
    void shouldAddPlayerToTurnCardOrder() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.returnTotemToTurnCard("alice");
        assertTrue(state.getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldRemovePlayerFromTrackWhenReturning() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.returnTotemToTurnCard("alice");
        assertFalse(state.getOfferTrackPositions().containsValue("alice"));
    }

    @Test
    void shouldNotRemoveOtherPlayersFromTrack() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        // aggiungi bob sulla track prima
        state.placeTotemOnTrack('B', "bob");
        state.returnTotemToTurnCard("alice");
        assertEquals("bob", state.getOfferTrackPositions().get('B'));
    }

    // --- updateOfferTurnCardOrder ---

    @Test
    void shouldReplaceEntireTurnCardOrder() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.updateOfferTurnCardOrder(List.of("bob", "alice"));
        assertEquals(List.of("bob", "alice"), state.getOfferTurnCardOrder());
    }

    // --- setPhase ---

    @Test
    void shouldUpdateCurrentPhase() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.setPhase("PlayerOfferPhase");
        assertEquals("PlayerOfferPhase", state.getCurrentPhase());
    }

    // --- incrementTurn ---

    @Test
    void shouldUpdateCurrentTurn() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        state.incrementTurn(3);
        assertEquals(3, state.getCurrentTurn());
    }

    // --- immutabilità getters ---

    @Test
    void shouldReturnUnmodifiableUpperRow() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> state.getUpperRowCardIds().add("hacked"));
    }

    @Test
    void shouldReturnUnmodifiableOfferTrack() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> state.getOfferTrackPositions().put('Z', "hacker"));
    }

    @Test
    void shouldReturnUnmodifiableTurnCardOrder() {
        ClientGameState state = ClientGameState.fromSnapshot(mockSnapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> state.getOfferTurnCardOrder().add("hacked"));
    }
}