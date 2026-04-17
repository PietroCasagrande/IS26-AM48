package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MesosServerTest {

    private MesosServer server;
    private GameManager mockGameManager;
    private Game mockGame;
    private PlayerContext mockPlayerContext;

    @BeforeEach
    void setUp() {
        mockGameManager   = mock(GameManager.class);
        mockGame          = mock(Game.class);
        mockPlayerContext = mock(PlayerContext.class);
        server            = new MesosServer(mockGameManager);

        when(mockGame.getPlayerContext()).thenReturn(mockPlayerContext);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Player mockPlayer(String nickname) {
        Player p = mock(Player.class);
        when(p.getNickname()).thenReturn(nickname);
        return p;
    }

    // ── registerClient ───────────────────────────────────────────────────────

    @Test
    @DisplayName("registerClient: registered view should receive subsequent broadcastToGame")
    void shouldDeliverDeltaToRegisteredClient() throws Exception {
        Player p1 = mockPlayer("P1");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1));

        VirtualView view = mock(VirtualView.class);
        server.registerClient("P1", view);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame("P1", delta);

        verify(view).showGameDelta(delta);
    }

    @Test
    @DisplayName("registerClient: overwriting same nickname replaces the view")
    void shouldReplaceViewOnSameNickname() throws Exception {
        Player p1 = mockPlayer("P1");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1));

        VirtualView oldView = mock(VirtualView.class);
        VirtualView newView = mock(VirtualView.class);
        server.registerClient("P1", oldView);
        server.registerClient("P1", newView);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame("P1", delta);

        verify(oldView, never()).showGameDelta(any());
        verify(newView).showGameDelta(delta);
    }

    // ── broadcastToGame ──────────────────────────────────────────────────────

    @Test
    @DisplayName("broadcastToGame: should call showGameDelta on every player in game")
    void shouldBroadcastDeltaToAllPlayersInGame() throws Exception {
        Player p1 = mockPlayer("P1");
        Player p2 = mockPlayer("P2");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1, p2));

        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        GameDelta delta = mock(GameDelta.class);
        server.broadcastToGame("P1", delta);

        verify(view1).showGameDelta(delta);
        verify(view2).showGameDelta(delta);
    }

    @Test
    @DisplayName("broadcastToGame: player not in connectedPlayers should be skipped silently")
    void shouldSkipPlayersNotConnected() throws Exception {
        Player p1 = mockPlayer("P1");
        Player p2 = mockPlayer("P2"); // non registrato
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1, p2));

        VirtualView view1 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        // P2 not registered, thus if view == null it mustn't be considered without exceptions

        assertDoesNotThrow(() -> server.broadcastToGame("P1", mock(GameDelta.class)));
        verify(view1).showGameDelta(any());
    }

    @Test
    @DisplayName("broadcastToGame: exception on one view should not stop broadcast to others")
    void shouldContinueBroadcastIfOneViewThrows() throws Exception {
        Player p1 = mockPlayer("P1");
        Player p2 = mockPlayer("P2");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1, p2));

        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        doThrow(new RuntimeException("disconnesso")).when(view1).showGameDelta(any());
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        assertDoesNotThrow(() -> server.broadcastToGame("P1", mock(GameDelta.class)));
        verify(view2).showGameDelta(any()); // P2 riceve comunque
    }

    // ── broadcastSnapshotToGame ──────────────────────────────────────────────

    @Test
    @DisplayName("broadcastSnapshotToGame: should call showInitialSnapshot on every player in game")
    void shouldBroadcastSnapshotToAllPlayersInGame() throws Exception {
        Player p1 = mockPlayer("P1");
        Player p2 = mockPlayer("P2");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1, p2));

        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        GameSnapshot snapshot = mock(GameSnapshot.class);
        server.broadcastSnapshotToGame("P1", snapshot);

        verify(view1).showInitialSnapshot(snapshot);
        verify(view2).showInitialSnapshot(snapshot);
    }

    @Test
    @DisplayName("broadcastSnapshotToGame: exception on one view should not stop broadcast to others")
    void shouldContinueSnapshotBroadcastIfOneViewThrows() throws Exception {
        Player p1 = mockPlayer("P1");
        Player p2 = mockPlayer("P2");
        when(mockGameManager.getGameByNickname("P1")).thenReturn(mockGame);
        when(mockPlayerContext.getPlayers()).thenReturn(List.of(p1, p2));

        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        doThrow(new RuntimeException("disconnesso")).when(view1).showInitialSnapshot(any());
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        assertDoesNotThrow(() -> server.broadcastSnapshotToGame("P1", mock(GameSnapshot.class)));
        verify(view2).showInitialSnapshot(any());
    }

    // ── broadcastToAll ───────────────────────────────────────────────────────

    @Test
    @DisplayName("broadcastToAll: should call reportError on every connected player")
    void shouldBroadcastErrorToAllConnectedPlayers() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        server.broadcastToAll("Problemi del server");

        verify(view1).reportError("Problemi del server");
        verify(view2).reportError("Problemi del server");
    }

    @Test
    @DisplayName("broadcastToAll: exception on one view should not stop broadcast to others")
    void shouldContinueBroadcastToAllIfOneViewThrows() throws Exception {
        VirtualView view1 = mock(VirtualView.class);
        VirtualView view2 = mock(VirtualView.class);
        doThrow(new RuntimeException("disconnesso")).when(view1).reportError(any());
        server.registerClient("P1", view1);
        server.registerClient("P2", view2);

        assertDoesNotThrow(() -> server.broadcastToAll("errore globale"));
        verify(view2).reportError("errore globale");
    }

    @Test
    @DisplayName("broadcastToAll: no connected players should not throw")
    void shouldNotThrowWhenNoPlayersConnected() {
        assertDoesNotThrow(() -> server.broadcastToAll("messaggio vuoto"));
    }
}