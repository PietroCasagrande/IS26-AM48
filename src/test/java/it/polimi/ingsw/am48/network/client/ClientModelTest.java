package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ClientModelTest {

    private ClientModel clientModel;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errCapture;

    @BeforeEach
    void setUp() {
        clientModel = new ClientModel();
        errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    // --- notifyError ---

    @Test
    @DisplayName("notifyError: should print error message to stderr")
    void shouldPrintErrorToStderr() {
        clientModel.notifyError("connessione persa");
        assertTrue(errCapture.toString().contains("connessione persa"));
    }

    @Test
    @DisplayName("notifyError: should print empty string without throwing")
    void shouldHandleEmptyErrorMessage() {
        assertDoesNotThrow(() -> clientModel.notifyError(""));
    }

    @Test
    @DisplayName("notifyError: stderr output should contain the exact message passed")
    void shouldContainExactMessage() {
        String msg = "errore specifico XYZ-42";
        clientModel.notifyError(msg);
        assertTrue(errCapture.toString().contains(msg));
    }

    // --- applyDelta ---

    @Test
    @DisplayName("applyDelta: should not throw with a valid GameDelta")
    void shouldNotThrowOnValidDelta() {
        GameDelta delta = mock(GameDelta.class);
        assertDoesNotThrow(() -> clientModel.applyDelta(delta));
    }

    @Test
    @DisplayName("applyDelta: should not throw with null (stub ancora vuoto)")
    void shouldNotThrowOnNullDelta() {
        // rimuovere quando applyDelta sarà implementato — null non sarà più valido
        assertDoesNotThrow(() -> clientModel.applyDelta(null));
    }

    // --- setInitialState ---

    @Test
    @DisplayName("setInitialState: should not throw with a valid GameSnapshot")
    void shouldNotThrowOnValidSnapshot() {
        GameSnapshot snapshot = mock(GameSnapshot.class);
        assertDoesNotThrow(() -> clientModel.setInitialState(snapshot));
    }

    @Test
    @DisplayName("setInitialState: multiple calls should not throw (overwrite scenario)")
    void shouldHandleMultipleSetInitialStateCalls() {
        GameSnapshot first  = mock(GameSnapshot.class);
        GameSnapshot second = mock(GameSnapshot.class);
        assertDoesNotThrow(() -> {
            clientModel.setInitialState(first);
            clientModel.setInitialState(second);
        });
    }
}