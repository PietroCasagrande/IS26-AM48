package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualViewRmi;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;

import static org.mockito.Mockito.*;

// Test unitario con mock che verifica la logica:
// chi chiama cosa, in che ordine, cosa succede sulle callback
class RmiClientTest {

    private VirtualServerRmi serverMock;
    private ClientModel modelMock;
    private RmiClient rmiClient;

    @BeforeEach
    void setUp() throws RemoteException {
        serverMock = mock(VirtualServerRmi.class);
        modelMock = mock(ClientModel.class);
        // Usiamo un costruttore alternativo per il test,
        // perché quello standard fa lookup sul Registry
        rmiClient = new RmiClient(serverMock, modelMock);
    }

    @Test
    @DisplayName("joinGame should call connect then joinGame on server")
    void shouldConnectThenJoinGame() throws Exception {
        rmiClient.joinGame(3, "P1");

        verify(serverMock).connect(eq("P1"), any(VirtualViewRmi.class));
        verify(serverMock).joinGame(3, "P1");
    }

    @Test
    @DisplayName("placeTotem should delegate to server")
    void shouldDelegatePlaceTotem() throws Exception {
        rmiClient.placeTotem("P1", 'A');

        verify(serverMock).placeTotem("P1", 'A');
    }

    @Test
    @DisplayName("takeCard should delegate to server")
    void shouldDelegateTakeCard() throws Exception {
        rmiClient.takeCard("P1", "c01");

        verify(serverMock).takeCard("P1", "c01");
    }

    @Test
    @DisplayName("showGameDelta callback should update client model")
    void shouldUpdateModelOnGameDelta() throws RemoteException {
        GameDelta delta = mock(GameDelta.class);
        rmiClient.showGameDelta(delta);

        verify(modelMock).applyDelta(delta);
    }

    @Test
    @DisplayName("showInitialSnapshot callback should set initial state")
    void shouldSetInitialStateOnSnapshot() throws RemoteException {
        GameSnapshot snapshot = mock(GameSnapshot.class);
        rmiClient.showInitialSnapshot(snapshot);

        verify(modelMock).setInitialState(snapshot);
    }

    @Test
    @DisplayName("reportError callback should notify error on model")
    void shouldNotifyErrorOnModel() throws RemoteException {
        rmiClient.reportError("test error");

        verify(modelMock).notifyError("test error");
    }
}