package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualViewRmi;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;
import java.util.List;

import static org.mockito.Mockito.*;

// Tests the logic of connect(), joinGame(), placeTotem(), takeCard()
// It does so through mocks of the Controller and MesosServer
class RmiServerTest {

    private GameController controllerMock;
    private MesosServer mesosServerMock;
    private VirtualViewRmi clientCallbackMock;
    private RmiServer rmiServer;

    @BeforeEach
    void setUp() throws RemoteException {
        controllerMock = mock(GameController.class);
        mesosServerMock = mock(MesosServer.class);
        clientCallbackMock = mock(VirtualViewRmi.class);
        rmiServer = new RmiServer(controllerMock, mesosServerMock);
    }

    @Test
    @DisplayName("connect should register client in MesosServer with adapter")
    void shouldRegisterClientOnConnect() throws RemoteException {
        rmiServer.connect("P1", clientCallbackMock);

        verify(mesosServerMock).registerClient(eq("P1"), any(RmiViewAdapter.class));
    }

    @Test
    @DisplayName("joinGame when game not started should send snapshot only to caller")
    void shouldSendSnapshotToCallerWhenGameNotStarted() throws Exception {
        GameSnapshot snapshot = mock(GameSnapshot.class);
        JoinResult result = new JoinResult(snapshot, false);
        when(controllerMock.handleJoinGame(3, "P1")).thenReturn(result);

        rmiServer.connect("P1", clientCallbackMock);
        rmiServer.joinGame(3, "P1");

        verify(clientCallbackMock).showInitialSnapshot(snapshot);
        verify(mesosServerMock, never()).broadcastSnapshotToGame(any(), any());
    }

    @Test
    @DisplayName("joinGame when game started should broadcast snapshot to all players")
    void shouldBroadcastSnapshotWhenGameStarted() throws Exception {
        GameSnapshot snapshot = mock(GameSnapshot.class);
        JoinResult result = new JoinResult(snapshot, true);
        when(controllerMock.handleJoinGame(3, "P1")).thenReturn(result);
        when(controllerMock.getPlayersInGame("P1")).thenReturn(List.of("P1", "P2", "P3"));

        rmiServer.connect("P1", clientCallbackMock);
        rmiServer.joinGame(3, "P1");

        verify(mesosServerMock).broadcastSnapshotToGame(List.of("P1", "P2", "P3"), snapshot);
    }

    @Test
    @DisplayName("placeTotem should broadcast delta to all players in game")
    void shouldBroadcastDeltaOnPlaceTotem() throws Exception {
        GameDelta delta = mock(GameDelta.class);
        when(controllerMock.handlePlaceTotem("P1", 'A')).thenReturn(List.of(delta));
        when(controllerMock.getPlayersInGame("P1")).thenReturn(List.of("P1", "P2"));

        rmiServer.connect("P1", clientCallbackMock);
        rmiServer.placeTotem("P1", 'A');

        verify(mesosServerMock).broadcastToGame(List.of("P1", "P2"), delta);
    }

    @Test
    @DisplayName("takeCard should broadcast all deltas to all players in game")
    void shouldBroadcastAllDeltasOnTakeCard() throws Exception {
        GameDelta delta1 = mock(GameDelta.class);
        GameDelta delta2 = mock(GameDelta.class);
        when(controllerMock.handleTakeCard("P1", "c01")).thenReturn(List.of(delta1, delta2));
        when(controllerMock.getPlayersInGame("P1")).thenReturn(List.of("P1", "P2"));

        rmiServer.connect("P1", clientCallbackMock);
        rmiServer.takeCard("P1", "c01");

        verify(mesosServerMock).broadcastToGame(List.of("P1", "P2"), delta1);
        verify(mesosServerMock).broadcastToGame(List.of("P1", "P2"), delta2);
    }

    @Test
    @DisplayName("placeTotem exception should send error only to caller")
    void shouldSendErrorToCallerOnException() throws Exception {
        when(controllerMock.handlePlaceTotem("P1", 'X'))
                .thenThrow(new IllegalStateException("Invalid move"));

        rmiServer.connect("P1", clientCallbackMock);
        rmiServer.placeTotem("P1", 'X');

        verify(clientCallbackMock).reportError("Invalid move");
        verify(mesosServerMock, never()).broadcastToGame(any(), any());
    }
}