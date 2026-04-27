package it.polimi.ingsw.am48.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

class SocketServerHandlerTest {

    private ServerSocket serverSocket;
    private Socket serverSideConn;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private ClientModel mockModel;
    private SocketServerHandler handler;
    private ObjectMapper mapper;

    private ExecutorService acceptExecutor;
    private ExecutorService handlerExecutor;

    @BeforeEach
    void setUp() throws Exception {
        mapper       = new ObjectMapper();
        mockModel    = mock(ClientModel.class);
        serverSocket = new ServerSocket(0); // porta OS-assigned, evita conflitti

        acceptExecutor  = Executors.newSingleThreadExecutor();
        handlerExecutor = Executors.newSingleThreadExecutor();

        Future<Socket> acceptFuture = acceptExecutor.submit(() -> serverSocket.accept());

        handler = new SocketServerHandler("localhost", serverSocket.getLocalPort(), mockModel);

        serverSideConn = acceptFuture.get(2, TimeUnit.SECONDS);
        serverIn  = new BufferedReader(new InputStreamReader(serverSideConn.getInputStream()));
        serverOut = new PrintWriter(serverSideConn.getOutputStream(), true);
    }

    @AfterEach
    void tearDown() throws IOException {
        acceptExecutor.shutdownNow();
        handlerExecutor.shutdownNow();
        if (serverSideConn != null) serverSideConn.close();
        if (serverSocket   != null) serverSocket.close();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Legge il prossimo NetworkMessage dal lato "server" del test. */
    private NetworkMessage readFromClient() throws Exception {
        return mapper.readValue(serverIn.readLine(), NetworkMessage.class);
    }

    /** Invia un NetworkMessage dal lato "server" verso il handler. */
    private void sendToHandler(String type, Object payload) throws Exception {
        NetworkMessage msg = new NetworkMessage(type, mapper.valueToTree(payload));
        serverOut.println(mapper.writeValueAsString(msg));
    }

    /** Avvia handler.run() su thread separato. */
    private void startHandlerThread() {
        handlerExecutor.submit(handler);
    }

    // ── joinGame ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("joinGame: should send message with type 'joinGame'")
    void shouldSendJoinGameType() throws Exception {
        handler.joinGame(3, "Pietro");
        assertEquals("joinGame", readFromClient().getType());
    }

    @Test
    @DisplayName("joinGame: payload should contain numPlayers and nickname")
    void shouldSendJoinGamePayload() throws Exception {
        handler.joinGame(3, "Pietro");
        var payload = readFromClient().getPayload();
        assertEquals(3,        payload.get("numPlayers").asInt());
        assertEquals("Pietro", payload.get("nickname").asText());
    }

    // ── placeTotem ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("placeTotem: should send message with type 'placeTotem'")
    void shouldSendPlaceTotemType() throws Exception {
        handler.placeTotem("Pietro", 'A');
        assertEquals("placeTotem", readFromClient().getType());
    }

    @Test
    @DisplayName("placeTotem: payload should contain nickname and position as string")
    void shouldSendPlaceTotemPayload() throws Exception {
        handler.placeTotem("Pietro", 'A');
        var payload = readFromClient().getPayload();
        assertEquals("Pietro", payload.get("nickname").asText());
        assertEquals("A",      payload.get("position").asText());
    }

    // ── takeCard ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("takeCard: should send message with type 'takeCard'")
    void shouldSendTakeCardType() throws Exception {
        handler.takeCard("Pietro", "char_01");
        assertEquals("takeCard", readFromClient().getType());
    }

    @Test
    @DisplayName("takeCard: payload should contain nickname and cardId")
    void shouldSendTakeCardPayload() throws Exception {
        handler.takeCard("Pietro", "char_01");
        var payload = readFromClient().getPayload();
        assertEquals("Pietro",  payload.get("nickname").asText());
        assertEquals("char_01", payload.get("cardId").asText());
    }

    // ── run() — error ────────────────────────────────────────────────────────

    @Test
    @DisplayName("run: 'error' message should call model.notifyError with payload text")
    void shouldCallNotifyErrorOnErrorMessage() throws Exception {
        startHandlerThread();

        // payload dell'error è un testo semplice
        sendToHandler("error", "Not your turn");

        verify(mockModel, timeout(1000)).notifyError("Not your turn");
    }

    // ── run() — connection lost ───────────────────────────────────────────────

    @Test
    @DisplayName("run: closing server connection should call model.notifyError")
    void shouldNotifyErrorOnConnectionLost() throws Exception {
        startHandlerThread();

        serverSideConn.close(); // simula disconnessione improvvisa

        verify(mockModel, timeout(1000)).notifyError(anyString());
    }

    // ── run() — gameDelta / initialSnapshot ──────────────────────────────────

    @Test
    @DisplayName("run: 'gameDelta' message should call model.applyDelta")
    void shouldCallApplyDeltaOnGameDelta() throws Exception {
        startHandlerThread();

        sendToHandler("gameDelta", Map.of("type", "totemPlaced",
                "playerNickname", "Pietro",
                "tileId", "A"));

        verify(mockModel, timeout(1000)).applyDelta(any(GameDelta.class));
    }

    @Test
    @DisplayName("run: 'initialSnapshot' message should call model.setInitialState")
    void shouldCallSetInitialStateOnSnapshot() throws Exception {
        startHandlerThread();

        sendToHandler("initialSnapshot", Map.of("gameID", "GAME-1",
                "numPlayers", 3));

        verify(mockModel, timeout(1000)).setInitialState(any(GameSnapshot.class));
    }
}