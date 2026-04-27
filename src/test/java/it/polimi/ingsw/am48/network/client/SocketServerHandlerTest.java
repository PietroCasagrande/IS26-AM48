package it.polimi.ingsw.am48.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.messages.notifications.ServerNotification;
import it.polimi.ingsw.am48.network.messages.notifications.ErrorNotification;
import it.polimi.ingsw.am48.network.messages.notifications.GameDeltaNotification;
import it.polimi.ingsw.am48.network.messages.notifications.InitialSnapshotNotification;
import it.polimi.ingsw.am48.network.messages.commands.ClientCommand;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
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
        mapper = new ObjectMapper();
        mockModel = mock(ClientModel.class);
        serverSocket = new ServerSocket(0);

        acceptExecutor = Executors.newSingleThreadExecutor();
        handlerExecutor = Executors.newSingleThreadExecutor();

        Future<Socket> acceptFuture = acceptExecutor.submit(() -> serverSocket.accept());

        handler = new SocketServerHandler("localhost", serverSocket.getLocalPort(), mockModel);

        serverSideConn = acceptFuture.get(2, TimeUnit.SECONDS);
        serverIn = new BufferedReader(new InputStreamReader(serverSideConn.getInputStream()));
        serverOut = new PrintWriter(serverSideConn.getOutputStream(), true);
    }

    @AfterEach
    void tearDown() throws IOException {
        acceptExecutor.shutdownNow();
        handlerExecutor.shutdownNow();
        if (serverSideConn != null) serverSideConn.close();
        if (serverSocket != null) serverSocket.close();
    }

    private void startHandlerThread() {
        handlerExecutor.submit(handler);
    }

    /** Legge il prossimo ClientCommand dal lato "server" del test */
    private ClientCommand readCommandFromClient() throws Exception {
        return mapper.readValue(serverIn.readLine(), ClientCommand.class);
    }

    /** Invia una ServerNotification dal lato "server" verso il handler */
    private void sendNotificationToHandler(ServerNotification notification) throws Exception {
        serverOut.println(mapper.writeValueAsString(notification));
    }

    // --- Invio comandi (client → server) ---

    @Test
    @DisplayName("joinGame: should send JoinGameCommand with correct type")
    void shouldSendJoinGameCommand() throws Exception {
        handler.joinGame(3, "Pietro");
        String json = serverIn.readLine();
        assertTrue(json.contains("\"type\":\"joinGame\""));
        assertTrue(json.contains("\"numPlayers\":3"));
        assertTrue(json.contains("\"nickname\":\"Pietro\""));
    }

    @Test
    @DisplayName("joinGame: should deserialize as JoinGameCommand")
    void shouldDeserializeJoinGameCommand() throws Exception {
        handler.joinGame(3, "Pietro");
        ClientCommand cmd = readCommandFromClient();
        assertTrue(cmd instanceof it.polimi.ingsw.am48.network.messages.commands.JoinGameCommand);
    }

    @Test
    @DisplayName("placeTotem: should send PlaceTotemCommand with correct data")
    void shouldSendPlaceTotemCommand() throws Exception {
        handler.placeTotem("Pietro", 'A');
        String json = serverIn.readLine();
        assertTrue(json.contains("\"type\":\"placeTotem\""));
    }

    @Test
    @DisplayName("takeCard: should send TakeCardCommand with correct data")
    void shouldSendTakeCardCommand() throws Exception {
        handler.takeCard("Pietro", "char_01");
        String json = serverIn.readLine();
        assertTrue(json.contains("\"type\":\"takeCard\""));
        assertTrue(json.contains("\"cardId\":\"char_01\""));
    }

    // --- Ricezione notifiche (server → client) ---

    @Test
    @DisplayName("run: ErrorNotification should call model.notifyError")
    void shouldCallNotifyErrorOnErrorNotification() throws Exception {
        startHandlerThread();

        sendNotificationToHandler(new ErrorNotification("Not your turn"));

        verify(mockModel, timeout(1000)).notifyError("Not your turn");
    }

    @Test
    @DisplayName("run: GameDeltaNotification should call model.applyDelta")
    void shouldCallApplyDeltaOnGameDeltaNotification() throws Exception {
        startHandlerThread();

        GameDelta delta = new TotemPlacedDelta("P1", 'A', List.of("P2"));  // ← AGGIUNGI questa riga
        sendNotificationToHandler(new GameDeltaNotification(delta));

        verify(mockModel, timeout(1000)).applyDelta(any(GameDelta.class));
    }

    @Test
    @DisplayName("run: closing connection should call model.notifyError")
    void shouldNotifyErrorOnConnectionLost() throws Exception {
        startHandlerThread();

        serverSideConn.close();

        verify(mockModel, timeout(1000)).notifyError(anyString());
    }
}