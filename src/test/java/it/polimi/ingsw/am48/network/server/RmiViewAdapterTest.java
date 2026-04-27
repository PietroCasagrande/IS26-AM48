package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualViewRmi;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Verifica che l'adapter deleghi correttamente al callback, e che le chiamate siano asincrone
class RmiViewAdapterTest {
    private VirtualViewRmi callbackMock;
    private RmiViewAdapter adapter;

    @BeforeEach
    void setUp(){
        callbackMock = mock(VirtualViewRmi.class);
        adapter = new RmiViewAdapter(callbackMock);
    }

    @Test
    @DisplayName("showGameDelta should delegate to rmi callback")
    void shouldDelegateShowGameDelta() throws Exception {
        GameDelta delta = mock(GameDelta.class);
        adapter.showGameDelta(delta);

        verify(callbackMock, timeout(1000)).showGameDelta(delta);
    }

    @Test
    @DisplayName("showInitialSnapshot should delegate to rmi callback")
    void shouldDelegateShowInitialSnapshot() throws Exception {
        GameSnapshot snapshot = mock(GameSnapshot.class);
        adapter.showInitialSnapshot(snapshot);

        verify(callbackMock, timeout(1000)).showInitialSnapshot(snapshot);
    }

    @Test
    @DisplayName("reportError should delegate to rmi callback")
    void shouldDelegateReportError() throws Exception {
        adapter.reportError("test error");

        verify(callbackMock, timeout(1000)).reportError("test error");
    }

    @Test
    @DisplayName("exception on callback should not propagate to caller")
    void shouldNotPropagateExceptionFromCallback() throws Exception {
        doThrow(new RemoteException("disconnected")).when(callbackMock).showGameDelta(any());

        assertDoesNotThrow(() -> adapter.showGameDelta(mock(GameDelta.class)));
    }
}
