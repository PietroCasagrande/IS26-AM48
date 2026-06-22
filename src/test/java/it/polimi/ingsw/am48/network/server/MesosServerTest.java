package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MesosServerTest {

    // Test class rewritten entirely: MesosServer no longer needs GameManager and does not access the model directly
    // No need to mock Game, Player and PlayerContext, just pass the list of nicknames (recipients)

    private MesosServer server;

    @BeforeEach
    void setUp() {
        server = new MesosServer();
    }

    @Test
    @DisplayName("registerClient: registered view should receive subsequent broadcastToGame")
    void shouldDeliverDeltaToRegisteredClient() throws Exception {
        VirtualView view = mock(VirtualView.class);
        server.registerClient("P1", view);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame(List.of("P1"), delta);

        verify(view).showGameDelta(delta);
    }

    @Test
    @DisplayName("registerClient: overwriting same nickname replaces the view")
    void shouldReplaceViewOnSameNickname() throws Exception {
        VirtualView oldView = mock(VirtualView.class);
        VirtualView newView = mock(VirtualView.class);
        server.registerClient("P1", oldView);
        server.registerClient("P1", newView);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame(List.of("P1"), delta);

        verify(oldView, never()).showGameDelta(any());
        verify(newView).showGameDelta(delta);
    }

    @Test
    @DisplayName("broadcastToGame: should call showGameDelta on every player in list")
    void shouldBroadcastDeltaToAllPlayersInGame() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame(List.of("P1", "P2"), delta);

        verify(view1).showGameDelta(delta);
        verify(view2).showGameDelta(delta);
    }

    @Test
    @DisplayName("broadcastToGame: player not in connectedPlayers should be skipped silently")
    void shouldSkipPlayersNotConnected() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        server.registerClient("P1", view1);

        assertDoesNotThrow(() -> server.broadcastToGame(List.of("P1", "P2"), mock(GameDelta.class)));
        verify(view1).showGameDelta(any());
    }

    @Test
    @DisplayName("broadcastToGame: exception on one view should not stop broadcast to others")
    void shouldContinueBroadcastIfOneViewThrows() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        doThrow(new RuntimeException("disconnesso")).when(view1).showGameDelta(any());
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        assertDoesNotThrow(() -> server.broadcastToGame(List.of("P1", "P2"), mock(GameDelta.class)));
        verify(view2).showGameDelta(any());
    }

    @Test
    @DisplayName("broadcastSnapshotToGame: should call showInitialSnapshot on every player in list")
    void shouldBroadcastSnapshotToAllPlayersInGame() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        GameSnapshot snapshot = mock(GameSnapshot.class);
        server.broadcastSnapshotToGame(List.of("P1", "P2"), snapshot);

        verify(view1).showInitialSnapshot(snapshot);
        verify(view2).showInitialSnapshot(snapshot);
    }

    @Test
    @DisplayName("broadcastSnapshotToGame: exception on one view should not stop broadcast to others")
    void shouldContinueSnapshotBroadcastIfOneViewThrows() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        doThrow(new RuntimeException("disconnesso")).when(view1).showInitialSnapshot(any());
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        assertDoesNotThrow(() -> server.broadcastSnapshotToGame(List.of("P1", "P2"), mock(GameSnapshot.class)));
        verify(view2).showInitialSnapshot(any());
    }
}