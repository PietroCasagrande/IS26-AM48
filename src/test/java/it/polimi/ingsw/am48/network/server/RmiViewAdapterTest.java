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
        // no-operation Runnable: existent tests do not test the disconnection
        adapter = new RmiViewAdapter(callbackMock, () -> {});
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


    @Test
    @DisplayName("onDisconnect should be called once when callback throws RemoteException")
    void shouldCallOnDisconnectOnRemoteException() throws Exception {
        Runnable onDisconnect = mock(Runnable.class);
        RmiViewAdapter adapterWithDisconnect = new RmiViewAdapter(callbackMock, onDisconnect);
        doThrow(new RemoteException("disconnected")).when(callbackMock).showGameDelta(any());

        adapterWithDisconnect.showGameDelta(mock(GameDelta.class));

        verify(onDisconnect, timeout(1000).times(1)).run();
    }

    // tests the AtomicBoolean: if two or more callbacks fail in succession, runnable should not be called more than once
    @Test
    @DisplayName("onDisconnect should be called only once even if multiple callbacks fail")
    void shouldCallOnDisconnectOnlyOnce() throws Exception {
        Runnable onDisconnect = mock(Runnable.class);
        RmiViewAdapter adapterWithDisconnect = new RmiViewAdapter(callbackMock, onDisconnect);
        doThrow(new RemoteException()).when(callbackMock).showGameDelta(any());
        doThrow(new RemoteException()).when(callbackMock).reportError(any());

        adapterWithDisconnect.showGameDelta(mock(GameDelta.class));
        adapterWithDisconnect.reportError("error");

        verify(onDisconnect, timeout(1000).times(1)).run();
    }
}
