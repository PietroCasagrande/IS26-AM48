package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.*;
import it.polimi.ingsw.am48.model.snapshot.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

        PlayerContextSnapshot playerContextSnap = mock(PlayerContextSnapshot.class);
        when(snap.getPlayerContext()).thenReturn(playerContextSnap);
        when(playerContextSnap.getPlayers()).thenReturn(players);

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
        // the state is built correctly from the snapshot
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
        // applyDelta delegates to the delta's applyTo method
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
        // when the totem is placed on the track it disappears from the turn card
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
        // alice is on tile A, we remove her from the track and add her back to the turn card
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

    // --- END OF TURN: 2 deltas in the correct order ---

    @Test
    void shouldApplyCharacterPickedDeltaThenEndTurnDeltaInCorrectOrder() {
        // checks that applying the 2 deltas in the correct order yields a consistent final state
        model.setInitialState(mockSnapshot);

        // delta 1: the player draws a character card, the totem returns
        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "newCard",
                List.of("c10", "c11"), List.of("c12"),
                6, 5, true /* updated food=6, points=5, totemReturned=true*/,
                "EndTurn"
        );

        // delta 2: end of turn with food and points updated for everyone
        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),   // food after sustenance
                Map.of("alice", 5, "bob", 3),   // updated prestige
                List.of("c20", "c21"), List.of("c22"),
                List.of("b10"), List.of(), 8, "PlaceTotem", List.of(), 3
        );

        // apply in the correct order
        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);

        // check that the pick was applied
        assertTrue(model.getState().getPlayer("alice").getCharacterCardIds().contains("newCard"));

        // check that endturn overwrote food and points
        assertEquals(4, model.getState().getPlayer("alice").getFood());
        assertEquals(5, model.getState().getPlayer("alice").getPoints());
        assertEquals(2, model.getState().getPlayer("bob").getFood());
        assertEquals(3, model.getState().getPlayer("bob").getPoints());

        // check showed rows updated by endturn
        assertEquals(List.of("c20", "c21"), model.getState().getUpperRowCardIds());
        assertEquals(List.of("c22"), model.getState().getLowerRowCardIds());
        assertEquals(List.of("b10"), model.getState().getBuildingUpperIds());

        // check totem returned to the turn card
        assertTrue(model.getState().getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldApplyBuildingPickedDeltaThenEndTurnDeltaInCorrectOrder() {
        // same scenario but with a BuildingCard
        model.setInitialState(mockSnapshot);

        BuildingCardPickedDelta pickDelta = new BuildingCardPickedDelta(
                "alice", "newBuilding",
                List.of("b10"), List.of("b11"),
                1, 5, true /*food=1 after paying for the building, points=5, totemReturned*/, "EndTurn"
        );

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 0, "bob", 2),
                Map.of("alice", 5, "bob", 0),
                List.of("c20"), List.of("c21"),
                List.of("b10"), List.of(), 8, "PlaceTotem", List.of(), 3
        );

        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);

        // check building added
        assertTrue(model.getState().getPlayer("alice").getBuildingCardIds().contains("newBuilding"));

        // check endturn overwrote the values
        assertEquals(0, model.getState().getPlayer("alice").getFood());
        assertEquals(5, model.getState().getPlayer("alice").getPoints());

        // check totem returned
        assertTrue(model.getState().getOfferTurnCardOrder().contains("alice"));
    }

    @Test
    void shouldNotAffectOtherPlayersPickWhenApplyingPickDelta() {
        // alice's pick must not change bob's state
        model.setInitialState(mockSnapshot);

        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "newCard",
                List.of("c10"), List.of("c11"),
                6, 5, false, "EndTurn"
        );

        model.applyDelta(pickDelta);

        assertFalse(model.getState().getPlayer("bob").getCharacterCardIds().contains("newCard"));
        assertEquals(0, model.getState().getPlayer("bob").getFood());
    }

// --- END OF GAME: 3 deltas in the correct order ---

    @Test
    void shouldApplyAllThreeDeltasInCorrectOrderOnGameEnd() {
        // checks the full end-of-game flow: pick → endturn → endgame
        model.setInitialState(mockSnapshot);

        CharacterCardPickedDelta pickDelta = new CharacterCardPickedDelta(
                "alice", "lastCard",
                List.of(), List.of(),
                6, 5, true, "EndTurn"
        );

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7),
                List.of(), List.of(),
                List.of(), List.of(), 11, "EndGame", List.of(), 3
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18),
                "alice"
        );

        // apply in the correct order
        model.applyDelta(pickDelta);
        model.applyDelta(endTurnDelta);
        model.applyDelta(endGameDelta);

        // check card added by pickDelta
        assertTrue(model.getState().getPlayer("alice").getCharacterCardIds().contains("lastCard"));

        // check that endGameDelta overwrote the final points
        assertEquals(25, model.getState().getPlayer("alice").getPoints());
        assertEquals(18, model.getState().getPlayer("bob").getPoints());
    }

    @Test
    void shouldOverwriteEndTurnPointsWithEndGamePoints() {
        // endgame overwrites endturn's points with the final scores
        model.setInitialState(mockSnapshot);

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7), // points after events
                List.of(), List.of(), List.of(), List.of(), 11, "EndGame", List.of(), 3
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18), // final points after endgame computation
                "alice"
        );

        model.applyDelta(endTurnDelta);
        model.applyDelta(endGameDelta);

        // the final points must be the endgame ones, not the endturn ones
        assertEquals(25, model.getState().getPlayer("alice").getPoints());
        assertEquals(18, model.getState().getPlayer("bob").getPoints());
    }

    @Test
    void shouldApplyEndGameDeltaToAllPlayers() {
        // endgame updates the points of all players, not only the winner
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
        // if the deltas are applied in the wrong order the final state is different
        model.setInitialState(mockSnapshot);

        EndTurnDelta endTurnDelta = new EndTurnDelta(
                Map.of("alice", 4, "bob", 2),
                Map.of("alice", 10, "bob", 7),
                List.of("c20"), List.of(), List.of(), List.of(),
                11, "EndGame", List.of(), 3
        );

        EndGameDelta endGameDelta = new EndGameDelta(
                Map.of("alice", 25, "bob", 18),
                "alice"
        );

        // reversed order: endgame first, endturn after
        model.applyDelta(endGameDelta);
        model.applyDelta(endTurnDelta);

        // endturn overwrites endgame → wrong state
        assertEquals(10, model.getState().getPlayer("alice").getPoints()); // endturn overwrote
        assertNotEquals(25, model.getState().getPlayer("alice").getPoints());
    }

    @Test
    @DisplayName("saveSession should persist nickname and numPlayers")
    void shouldSaveSession() {
        ClientModel model = new ClientModel();
        model.saveSession("alice", 2);
        assertEquals("alice", model.getSessionNickname());
        assertEquals(2, model.getSessionNumPlayers());
    }

    @Test
    @DisplayName("getSessionNickname should return null before any join")
    void shouldReturnNullBeforeJoin() {
        ClientModel model = new ClientModel();
        assertNull(model.getSessionNickname());
    }

}
