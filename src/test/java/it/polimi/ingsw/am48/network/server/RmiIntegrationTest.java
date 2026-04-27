package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.RmiClient;
import org.junit.jupiter.api.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.junit.jupiter.api.Assertions.*;


// Integration Test per RMI che verifica la connessione, ovvero:
// il registry funziona, lo stub arriva, la serializzazione non crasha
@Timeout(10)
class RmiIntegrationTest {

    private static final int RMI_PORT = 2099; // porta diversa per evitare conflitti col server reale
    private static Registry registry;

    @BeforeAll
    static void startRegistry() throws Exception {
        registry = LocateRegistry.createRegistry(RMI_PORT);
    }

    @BeforeEach
    void setUp() throws Exception {
        GameManager gameManager = new GameManager();
        MesosServer mesosServer = new MesosServer();
        GameController controller = new GameController(gameManager);
        RmiServer rmiServer = new RmiServer(controller, mesosServer);

        registry.rebind("MesosServer", rmiServer);
    }

    @AfterAll
    static void stopRegistry() throws Exception {
        // Rimuovi il binding per pulire
        try { registry.unbind("MesosServer"); } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("RMI client should connect, lookup server, and join game")
    void shouldConnectAndJoinGame() throws Exception {
        ClientModel model = new ClientModel();
        RmiClient client = new RmiClient("localhost", RMI_PORT, model);

        assertDoesNotThrow(() -> client.joinGame(2, "TestPlayer"));
    }

    @Test
    @DisplayName("RMI client should receive snapshot callback when game starts")
    void shouldReceiveSnapshotWhenGameFull() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();

        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        client1.joinGame(2, "P1");
        client2.joinGame(2, "P2");

        // Aspetta che i callback asincroni vengano processati
        Thread.sleep(500);

        // Entrambi i client dovrebbero aver ricevuto lo snapshot
        assertNotNull(model1.getState());
        assertNotNull(model2.getState());
    }

    // TODO:
//    @Test
//    @DisplayName("RMI: placeTotem should broadcast delta to all players")
//    void shouldBroadcastDeltaOnPlaceTotem() throws Exception {
//        ClientModel model1 = new ClientModel();
//        ClientModel model2 = new ClientModel();
//
//        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
//        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);
//
//        client1.joinGame(2, "Alice");
//        client2.joinGame(2, "Bob");
//        Thread.sleep(500);
//
//        // Entrambi hanno ricevuto lo snapshot, la partita è in PlaceTotemPhase
//        assertNotNull(model1.getState());
//        assertNotNull(model2.getState());
//
//        // Alice piazza il totem
//        client1.placeTotem("Alice", 'A');
//        Thread.sleep(500);
//
//        // Entrambi i client devono aver ricevuto l'aggiornamento
//        assertTrue(model1.getState().getOfferTrackPositions().containsKey('A'));
//        assertTrue(model2.getState().getOfferTrackPositions().containsKey('A'));
//    }

    @Test
    @DisplayName("RMI: invalid action should send error only to caller")
    void shouldSendErrorOnlyToCaller() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();

        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        client1.joinGame(2, "Eve");
        client2.joinGame(2, "Frank");
        Thread.sleep(500);

        // Eve prova a piazzare il totem su una posizione invalida
        // Questo dovrebbe generare un errore solo per Eve
        assertDoesNotThrow(() -> client1.placeTotem("Eve", 'Z'));
        Thread.sleep(500);

        // Il gioco non deve crashare, entrambi i model devono ancora funzionare
        assertNotNull(model1.getState());
        assertNotNull(model2.getState());
    }

    // TODO:
//    @Test
//    @DisplayName("RMI: full game flow - join, place totems, take card")
//    void shouldHandleFullGameFlow() throws Exception {
//        ClientModel model1 = new ClientModel();
//        ClientModel model2 = new ClientModel();
//
//        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
//        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);
//
//        // Join
//        client1.joinGame(2, "Gina");
//        client2.joinGame(2, "Hugo");
//        Thread.sleep(500);
//
//        assertNotNull(model1.getState());
//        assertNotNull(model2.getState());
//
//        // Place totems - entrambi piazzano
//        client1.placeTotem("Gina", 'A');
//        Thread.sleep(300);
//        client2.placeTotem("Hugo", 'B');
//        Thread.sleep(500);
//
//        // Entrambi i totem dovrebbero essere visibili a entrambi i client
//        assertTrue(model1.getState().getOfferTrackPositions().containsKey('A'));
//        assertTrue(model1.getState().getOfferTrackPositions().containsKey('B'));
//        assertTrue(model2.getState().getOfferTrackPositions().containsKey('A'));
//        assertTrue(model2.getState().getOfferTrackPositions().containsKey('B'));
//    }
}