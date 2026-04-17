package it.polimi.ingsw.am48.network.server;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ServerMainTest {

    private static final int PORT = 12345;
    private ExecutorService executor;
    private ServerMain server;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
        server = null;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
        executor.shutdownNow();
    }

    // ── costruttore ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor: should instantiate without throwing")
    void shouldInstantiateWithoutThrowing() {
        assertDoesNotThrow(ServerMain::new);
    }

    @Test
    @DisplayName("constructor: multiple instances should not conflict at construction time")
    void shouldAllowMultipleInstances() {
        // il conflitto di porta avviene solo su start(), non sul costruttore
        assertDoesNotThrow(() -> {
            new ServerMain();
            new ServerMain();
        });
    }

    // ── start() — integrazione reale ─────────────────────────────────────────

    @Test
    @Timeout(5)
    @DisplayName("start: server should accept incoming TCP connections on PORT 12345")
    void shouldAcceptConnectionOnPort() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);

        // attesa per il server che sia pronto
        // waitForPort(PORT, 3000);

        // tenta connessione reale
        assertDoesNotThrow(() -> {
            try (Socket s = new Socket("localhost", PORT)) {
                assertTrue(s.isConnected());
            }
        });
    }

    @Test
    @Timeout(5)
    @DisplayName("start: server should accept multiple concurrent connections")
    void shouldAcceptMultipleConcurrentConnections() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);

        // attesa per il server che sia pronto
        // waitForPort(PORT, 3000);

        // apre 3 connessioni simultanee
        Socket s1 = new Socket("localhost", PORT);
        Socket s2 = new Socket("localhost", PORT);
        Socket s3 = new Socket("localhost", PORT);

        assertTrue(s1.isConnected());
        assertTrue(s2.isConnected());
        assertTrue(s3.isConnected());

        s1.close(); s2.close(); s3.close();
    }

    @Test
    @DisplayName("start: server should NOT be reachable before start() is called")
    void shouldNotBeReachableBeforeStart() {
        new ServerMain(); // costruisce ma non avvia

        assertThrows(ConnectException.class, () -> {
            try (Socket ignored = new Socket("localhost", PORT)) { }
        });
    }

    @Test
    void shouldHandleJoinGameMessage() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);

        try (Socket s = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            // Invia un messaggio JSON reale per coprire handleMessage()
            String joinMsg = "{\"type\":\"joinGame\",\"payload\":{\"nickname\":\"StudentPolimi\",\"numPlayers\":2}}";
            out.println(joinMsg);

            // Verifica la risposta (copre sendMessage e VirtualViewSocket)
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("initialSnapshot"));
        }
    }
}