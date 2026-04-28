package it.polimi.ingsw.am48.network.server;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private void waitForPort(int port, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try (Socket s = new Socket("localhost", port)) {
                return;
            } catch (ConnectException e) {
                Thread.sleep(50);
            }
        }
        fail("Server non pronto entro " + timeoutMs + "ms");
    }

    @Test
    @Order(1)
    @DisplayName("constructor: should instantiate without throwing")
    void shouldInstantiateWithoutThrowing() {
        assertDoesNotThrow(ServerMain::new);
    }

    @Test
    @Order(2)
    @Timeout(5)
    @DisplayName("start: server should accept incoming TCP connections on PORT 12345")
    void shouldAcceptConnectionOnPort() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);
        waitForPort(PORT, 3000);

        try (Socket s = new Socket("localhost", PORT)) {
            assertTrue(s.isConnected());
        }
    }

    @Test
    @Order(3)
    @Timeout(5)
    @DisplayName("start: server should accept multiple concurrent connections")
    void shouldAcceptMultipleConcurrentConnections() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);
        waitForPort(PORT, 3000);

        Socket s1 = new Socket("localhost", PORT);
        Socket s2 = new Socket("localhost", PORT);
        Socket s3 = new Socket("localhost", PORT);

        assertTrue(s1.isConnected());
        assertTrue(s2.isConnected());
        assertTrue(s3.isConnected());

        s1.close(); s2.close(); s3.close();
    }

    @Test
    @Order(4)
    @Timeout(5)
    @DisplayName("start: should handle joinGame command with new format")
    void shouldHandleJoinGameMessage() throws Exception {
        server = new ServerMain();
        executor.submit(server::start);
        waitForPort(PORT, 3000);

        try (Socket s = new Socket("localhost", PORT);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            String joinMsg = "{\"type\":\"joinGame\",\"numPlayers\":2,\"nickname\":\"StudentPolimi\"}";
            out.println(joinMsg);

            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("initialSnapshot"));
        }
    }
}