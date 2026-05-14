package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.network.client.RmiClient;
import it.polimi.ingsw.am48.network.client.SocketServerHandler;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;
import org.junit.jupiter.api.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Timeout(30)
class GameFlowIntegrationTest {

    private static final int RMI_PORT = 3099;
    private static final int SOCKET_PORT = 3100;
    private static Registry registry;
    private ExecutorService socketExecutor;
    private ServerSocket serverSocket;

    @BeforeAll
    static void startRegistry() throws Exception {
        registry = LocateRegistry.createRegistry(RMI_PORT);
    }

    @BeforeEach
    void setUp() throws Exception {
        GameRepository mockRepo = mock(GameRepository.class);
        when(mockRepo.listActiveGameIds()).thenReturn(List.of());
        LeaderboardRepository mockLeaderboard = mock(LeaderboardRepository.class);
        GameManager gameManager = new GameManager(mockRepo, mockLeaderboard);

        MesosServer mesosServer = new MesosServer();
        GameController controller = new GameController(gameManager);

        // RMI
        RmiServer rmiServer = new RmiServer(controller, mesosServer);
        registry.rebind("MesosServer", rmiServer);

        // Socket
        socketExecutor = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(SOCKET_PORT);
        socketExecutor.submit(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    SocketClientHandler handler = new SocketClientHandler(clientSocket, controller, mesosServer);
                    socketExecutor.execute(handler);
                }
            } catch (Exception e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        try { serverSocket.close(); } catch (Exception ignored) {}
        socketExecutor.shutdownNow();
    }

    // === HELPERS ===

    private RmiClient createRmiClient(ClientModel model) throws Exception {
        return new RmiClient("localhost", RMI_PORT, model);
    }

    private SocketServerHandler createSocketClient(ClientModel model) throws Exception {
        SocketServerHandler handler = new SocketServerHandler("localhost", SOCKET_PORT, model);
        new Thread(handler).start();
        Thread.sleep(100); // aspetta che il thread di ascolto sia attivo
        return handler;
    }

    /** Legge l'ordine dei turni e mappa nickname → client */
    private List<String> getTurnOrder(ClientModel model) {
        return model.getState().getOfferTurnCardOrder();
    }

    /** Piazza i totem nell'ordine corretto per una partita da 2 */
    private void placeTotemsInOrder(
            ClientModel referenceModel,
            Map<String, VirtualServer> clientByNick,
            char[] positions) throws Exception {

        List<String> order = getTurnOrder(referenceModel);
        for (int i = 0; i < order.size(); i++) {
            String nick = order.get(i);
            clientByNick.get(nick).placeTotem(nick, positions[i]);
            Thread.sleep(300);
        }
        Thread.sleep(500);
    }

    // ==========================================================
    // TEST 4.1 — Due partite in parallelo
    // ==========================================================

    @Test
    @DisplayName("4.1: Two parallel games should not interfere with each other")
    void twoParallelGamesShouldNotInterfere() throws Exception {
        // --- Partita 1: Alice e Bob ---
        ClientModel modelA = new ClientModel();
        ClientModel modelB = new ClientModel();
        RmiClient clientA = createRmiClient(modelA);
        RmiClient clientB = createRmiClient(modelB);

        clientA.joinGame(2, "Alice");
        clientB.joinGame(2, "Bob");
        Thread.sleep(500);

        // --- Partita 2: Charlie e Dave ---
        ClientModel modelC = new ClientModel();
        ClientModel modelD = new ClientModel();
        RmiClient clientC = createRmiClient(modelC);
        RmiClient clientD = createRmiClient(modelD);

        clientC.joinGame(2, "Charlie");
        clientD.joinGame(2, "Dave");
        Thread.sleep(500);

        // Entrambe le partite sono avviate
        assertNotNull(modelA.getState());
        assertNotNull(modelB.getState());
        assertNotNull(modelC.getState());
        assertNotNull(modelD.getState());

        // Le partite hanno gameId diversi
        assertNotEquals(modelA.getState().getGameId(), modelC.getState().getGameId(),
                "Parallel games should have different gameIds");

        // Le carte esposte possono essere diverse (mazzi shufflati)
        // System.out.println("[4.1] Game1 upper: " + modelA.getState().getUpperRowCardIds());
        // System.out.println("[4.1] Game2 upper: " + modelC.getState().getUpperRowCardIds());

        // --- Azioni nella partita 1 ---
        Map<String, VirtualServer> game1Clients = Map.of("Alice", clientA, "Bob", clientB);
        placeTotemsInOrder(modelA, game1Clients, new char[]{'B', 'C'});

        // La partita 1 ha totem piazzati
        assertTrue(modelA.getState().getOfferTrackPositions().containsKey('B'));
        assertTrue(modelA.getState().getOfferTrackPositions().containsKey('C'));

        // La partita 2 NON è stata toccata
        assertTrue(modelC.getState().getOfferTrackPositions().isEmpty(),
                "Game 2 track should be empty — Game 1 actions should not leak");
        assertTrue(modelD.getState().getOfferTrackPositions().isEmpty(),
                "Game 2 track should be empty — Game 1 actions should not leak");

        // --- Azioni nella partita 2 ---
        Map<String, VirtualServer> game2Clients = Map.of("Charlie", clientC, "Dave", clientD);
        placeTotemsInOrder(modelC, game2Clients, new char[]{'E', 'F'});

        // La partita 2 ha totem piazzati su posizioni diverse
        assertTrue(modelC.getState().getOfferTrackPositions().containsKey('E'));
        assertTrue(modelC.getState().getOfferTrackPositions().containsKey('F'));

        // La partita 1 non è cambiata
        assertEquals(2, modelA.getState().getOfferTrackPositions().size(),
                "Game 1 should still have exactly 2 totems");
        assertFalse(modelA.getState().getOfferTrackPositions().containsKey('E'),
                "Game 1 should not have Game 2's positions");
    }

    // ==========================================================
    // TEST 4.2 — Protocollo misto (Socket + RMI nella stessa partita)
    // ==========================================================

    @Test
    @DisplayName("4.2: Socket and RMI clients in the same game should see identical state")
    void mixedProtocolShouldWork() throws Exception {
        // Alice su RMI
        ClientModel modelAlice = new ClientModel();
        RmiClient clientAlice = createRmiClient(modelAlice);

        // Bob su Socket
        ClientModel modelBob = new ClientModel();
        SocketServerHandler clientBob = createSocketClient(modelBob);

        // Join
        clientAlice.joinGame(2, "Alice");
        Thread.sleep(200);
        clientBob.joinGame(2, "Bob");
        Thread.sleep(500);

        // Entrambi ricevono lo snapshot
        assertNotNull(modelAlice.getState(), "RMI client should have state");
        assertNotNull(modelBob.getState(), "Socket client should have state");

        // Stessi dati dopo il join
        assertEquals(modelAlice.getState().getGameId(), modelBob.getState().getGameId(),
                "Both clients should be in the same game");
        assertEquals(modelAlice.getState().getUpperRowCardIds(), modelBob.getState().getUpperRowCardIds(),
                "Upper row should be identical across protocols");
        assertEquals(modelAlice.getState().getLowerRowCardIds(), modelBob.getState().getLowerRowCardIds(),
                "Lower row should be identical across protocols");
        assertEquals(modelAlice.getState().getOfferTurnCardOrder(), modelBob.getState().getOfferTurnCardOrder(),
                "Turn order should be identical across protocols");

        // System.out.println("[4.2] Game: " + modelAlice.getState().getGameId());
        // System.out.println("[4.2] Turn order: " + modelAlice.getState().getOfferTurnCardOrder());

        // Place totems
        Map<String, VirtualServer> clientByNick = new HashMap<>();
        clientByNick.put("Alice", clientAlice);
        clientByNick.put("Bob", clientBob);

        placeTotemsInOrder(modelAlice, clientByNick, new char[]{'B', 'C'});

        // Entrambi vedono la stessa track
        assertEquals(modelAlice.getState().getOfferTrackPositions(),
                modelBob.getState().getOfferTrackPositions(),
                "Offer track should be identical across protocols after totem placement");

        // System.out.println("[4.2] Track (RMI): " + modelAlice.getState().getOfferTrackPositions());
        // System.out.println("[4.2] Track (Socket): " + modelBob.getState().getOfferTrackPositions());

        // Take card — il primo nell'ordine di offerta prende dalla lower row
        String firstPicker = modelAlice.getState().getOfferTrackPositions().get('B');
        List<String> lowerCards = modelAlice.getState().getLowerRowCardIds();
        assertFalse(lowerCards.isEmpty(), "Lower row should have cards");
        String cardToPick = lowerCards.get(0);

        // System.out.println("[4.2] " + firstPicker + " picks: " + cardToPick);
        clientByNick.get(firstPicker).takeCard(firstPicker, cardToPick);
        Thread.sleep(500);

        // Entrambi vedono la carta sparire dalle righe
        assertFalse(modelAlice.getState().getLowerRowCardIds().contains(cardToPick),
                "Card should be gone from RMI client's lower row");
        assertFalse(modelBob.getState().getLowerRowCardIds().contains(cardToPick),
                "Card should be gone from Socket client's lower row");

        // Entrambi vedono la carta nella tribù del giocatore
        assertEquals(
                modelAlice.getState().getPlayer(firstPicker).getCharacterCardIds(),
                modelBob.getState().getPlayer(firstPicker).getCharacterCardIds(),
                "Player's tribe should be identical across protocols");

        // Stato complessivo identico
        assertEquals(modelAlice.getState().getUpperRowCardIds(), modelBob.getState().getUpperRowCardIds(),
                "Upper row should remain identical across protocols");
    }

    // ==========================================================
    // TEST 4.3 — Azioni rapide in sequenza
    // ==========================================================

    @Test
    @DisplayName("4.3: Rapid totem placements should all be processed correctly")
    void rapidActionsShouldNotCorruptState() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();
        RmiClient client1 = createRmiClient(model1);
        RmiClient client2 = createRmiClient(model2);

        client1.joinGame(2, "Fast");
        client2.joinGame(2, "Furious");
        Thread.sleep(500);

        List<String> order = getTurnOrder(model1);
        Map<String, VirtualServer> clients = Map.of("Fast", client1, "Furious", client2);

        // Piazza senza sleep tra le due azioni
        String first = order.get(0);
        String second = order.get(1);
        clients.get(first).placeTotem(first, 'B');
        clients.get(second).placeTotem(second, 'C');
        // Nessun Thread.sleep tra le due!

        Thread.sleep(1000); // aspetta che tutto venga processato

        // Entrambe le posizioni devono essere occupate
        assertTrue(model1.getState().getOfferTrackPositions().containsKey('B'),
                "Position B should be occupied after rapid placement");
        assertTrue(model1.getState().getOfferTrackPositions().containsKey('C'),
                "Position C should be occupied after rapid placement");

        // Entrambi i client in sync
        assertEquals(model1.getState().getOfferTrackPositions(),
                model2.getState().getOfferTrackPositions(),
                "Both clients should see identical track after rapid actions");
    }
}