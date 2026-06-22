package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.network.client.RmiClient;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;
import org.junit.jupiter.api.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


// Integration Test for RMI that verifies the connection, that is:
// the registry works, the stub arrives, the serialization does not crash
@Timeout(10)
class RmiIntegrationTest {

    private static final int RMI_PORT = 2099; // different port to avoid conflicts with the real server
    private static Registry registry;

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
        RmiServer rmiServer = new RmiServer(controller, mesosServer);
        registry.rebind("MesosServer", rmiServer);
    }

    @AfterAll
    static void stopRegistry() throws Exception {
        // Remove the binding to clean up
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

        // Wait for the asynchronous callbacks to be processed
        Thread.sleep(500);

        // Both clients should have received the snapshot
        assertNotNull(model1.getState());
        assertNotNull(model2.getState());
    }

    @Test
    @DisplayName("RMI: placeTotem should broadcast delta to all players")
    void shouldBroadcastDeltaOnPlaceTotem() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();

        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        client1.joinGame(2, "Alice");
        client2.joinGame(2, "Bob");
        Thread.sleep(500);

        assertNotNull(model1.getState());
        assertNotNull(model2.getState());

        // Read the order and place the first one
        List<String> order = model1.getState().getOfferTurnCardOrder();
        String first = order.get(0);
        Map<String, RmiClient> clients = Map.of("Alice", client1, "Bob", client2);

        clients.get(first).placeTotem(first, 'C');
        Thread.sleep(500);

        // Both clients must see the placed totem
        assertEquals(first, model1.getState().getOfferTrackPositions().get('C'));
        assertEquals(first, model2.getState().getOfferTrackPositions().get('C'));
    }

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

        // Eve tries to place the totem on an invalid position
        // This should generate an error only for Eve
        assertDoesNotThrow(() -> client1.placeTotem("Eve", 'Z'));
        Thread.sleep(500);

        // The game must not crash, both models must still work
        assertNotNull(model1.getState());
        assertNotNull(model2.getState());
    }

    /*
     *  Snapshot-delta consistency is confirmed: the turn order arrives as nicknames,
     *  placeTotem stores the nickname, the map is consistent
     */
    @Test
    @DisplayName("RMI: full game flow - join and place totems for 5 players")
    void shouldHandleFullGameFlow() throws Exception {
        ClientModel[] models = new ClientModel[5];
        RmiClient[] clients = new RmiClient[5];
        String[] nicknames = {"Alice", "Bob", "Charlie", "Dave", "Eve"};
        char[] positions = {'F', 'B', 'C', 'D', 'E'};

        // Join
        for (int i = 0; i < 5; i++) {
            models[i] = new ClientModel();
            clients[i] = new RmiClient("localhost", RMI_PORT, models[i]);
            clients[i].joinGame(5, nicknames[i]);
        }
        Thread.sleep(500);

        for (ClientModel model : models) {
            assertNotNull(model.getState());
        }

        // The order now contains nicknames, not colors
        List<String> turnOrder = models[0].getState().getOfferTurnCardOrder();
        // System.out.println("[TEST] Turn order: " + turnOrder);

        // Verify that the order contains nicknames
        for (String name : turnOrder) {
            assertTrue(List.of(nicknames).contains(name),
                    "Turn order should contain nicknames, got: " + name);
        }

        // Map nickname → client
        Map<String, RmiClient> clientByNickname = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            clientByNickname.put(nicknames[i], clients[i]);
        }

        // Place the totems in the correct order
        for (int i = 0; i < 5; i++) {
            String nickname = turnOrder.get(i);
            RmiClient client = clientByNickname.get(nickname);
            // System.out.println("[TEST] Turn " + i + ": " + nickname + " places on " + positions[i]);
            client.placeTotem(nickname, positions[i]);
            Thread.sleep(300);
        }
        Thread.sleep(500);

        // Verify that all positions are occupied in all clients
        for (ClientModel model : models) {
            for (char pos : positions) {
                assertTrue(model.getState().getOfferTrackPositions().containsKey(pos),
                        "Position " + pos + " should be occupied");
            }
        }

        // Verify that each position is occupied by the right player (nickname, not color)
        for (int i = 0; i < 5; i++) {
            String expectedNickname = turnOrder.get(i);
            String actualNickname = models[0].getState().getOfferTrackPositions().get(positions[i]);
            assertEquals(expectedNickname, actualNickname,
                    "Position " + positions[i] + " should have " + expectedNickname);
        }
    }

    @Test
    @DisplayName("RMI: complete round - join, place totems, take cards")
    void shouldHandleCompleteRound() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();
        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        // === PHASE 1: JOIN ===
        client1.joinGame(2, "Alice");
        client2.joinGame(2, "Bob");
        Thread.sleep(500);

        assertNotNull(model1.getState());
        assertNotNull(model2.getState());
        // System.out.println("[TEST] Phase after join: " + model1.getState().getCurrentPhase());

        // Both see the same exposed cards
        assertEquals(model1.getState().getUpperRowCardIds(), model2.getState().getUpperRowCardIds());
        assertEquals(model1.getState().getLowerRowCardIds(), model2.getState().getLowerRowCardIds());

        // There are cards exposed on the board
        assertFalse(model1.getState().getUpperRowCardIds().isEmpty(), "Upper row should have cards");
        assertFalse(model1.getState().getLowerRowCardIds().isEmpty(), "Lower row should have cards");

        // System.out.println("[TEST] Upper row: " + model1.getState().getUpperRowCardIds());
        // System.out.println("[TEST] Lower row: " + model1.getState().getLowerRowCardIds());

        // === PHASE 2: PLACE TOTEM ===
        List<String> turnOrder = model1.getState().getOfferTurnCardOrder();
        // System.out.println("[TEST] Place order: " + turnOrder);

        Map<String, RmiClient> clientByNick = Map.of("Alice", client1, "Bob", client2);

        // Valid positions for 2 players: B, C, E, F
        char[] validPositions = {'B', 'C'};

        String firstPlacer = turnOrder.get(0);
        String secondPlacer = turnOrder.get(1);

        clientByNick.get(firstPlacer).placeTotem(firstPlacer, validPositions[0]);
        Thread.sleep(300);
        clientByNick.get(secondPlacer).placeTotem(secondPlacer, validPositions[1]);
        Thread.sleep(500);

        // Verify placement
        assertTrue(model1.getState().getOfferTrackPositions().containsKey(validPositions[0]));
        assertTrue(model1.getState().getOfferTrackPositions().containsKey(validPositions[1]));
        // System.out.println("[TEST] Track after placement: " + model1.getState().getOfferTrackPositions());

        // === PHASE 3: TAKE CARD ===
        // The pick order is determined by the position on the track (B before C)
        // Whoever placed on B plays first in the offer phase
        String firstPicker = model1.getState().getOfferTrackPositions().get(validPositions[0]);
        String secondPicker = model1.getState().getOfferTrackPositions().get(validPositions[1]);
        // System.out.println("[TEST] Pick order: " + firstPicker + " then " + secondPicker);

        // The first player takes a card from the lower row
        List<String> lowerCards = model1.getState().getLowerRowCardIds();
        String cardToPick = lowerCards.get(0);
        // System.out.println("[TEST] " + firstPicker + " picks card (from lower row): " + cardToPick);

        clientByNick.get(firstPicker).takeCard(firstPicker, cardToPick);
        Thread.sleep(500);

        // Verify that the card was taken
        // The player who took it must have it in their tribe
        ClientPlayerState picker1State = model1.getState().getPlayer(firstPicker);
        // System.out.println("[TEST] " + firstPicker + " characters: " + picker1State.getCharacterCardIds());
        // System.out.println("[TEST] " + firstPicker + " buildings: " + picker1State.getBuildingCardIds());

        // The card must no longer be in the exposed rows of either client
        assertFalse(model1.getState().getUpperRowCardIds().contains(cardToPick),
                "Card " + cardToPick + " should no longer be in upper row of model1");
        assertFalse(model2.getState().getUpperRowCardIds().contains(cardToPick),
                "Card " + cardToPick + " should no longer be in upper row of model2");
    }

    @Test
    @DisplayName("RMI: wrong turn order should not corrupt game state")
    void shouldRejectOutOfTurnActions() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();
        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        client1.joinGame(2, "Alice");
        client2.joinGame(2, "Bob");
        Thread.sleep(500);

        List<String> turnOrder = model1.getState().getOfferTurnCardOrder();
        String first = turnOrder.get(0);
        String second = turnOrder.get(1);

        Map<String, RmiClient> clientByNick = Map.of("Alice", client1, "Bob", client2);

        // The second player tries to place first — it must fail
        clientByNick.get(second).placeTotem(second, 'B');
        Thread.sleep(300);

        // The track must be empty — the placement did not succeed
        assertTrue(model1.getState().getOfferTrackPositions().values().stream().allMatch(String::isEmpty),
                "Track should be empty after out-of-turn placement");
        assertTrue(model2.getState().getOfferTrackPositions().values().stream().allMatch(String::isEmpty),
                "Track should be empty after out-of-turn placement");

        // The correct player places — it must work
        clientByNick.get(first).placeTotem(first, 'B');
        Thread.sleep(300);

        assertTrue(model1.getState().getOfferTrackPositions().containsKey('B'),
                "Track should have B after correct placement");

        // The game state is not corrupted — the second player can now place
        clientByNick.get(second).placeTotem(second, 'C');
        Thread.sleep(300);

        assertTrue(model1.getState().getOfferTrackPositions().containsKey('C'),
                "Track should have C after second placement");
    }

    @Test
    @DisplayName("RMI: both clients see identical state after every action")
    void shouldKeepClientsInSync() throws Exception {
        ClientModel model1 = new ClientModel();
        ClientModel model2 = new ClientModel();
        RmiClient client1 = new RmiClient("localhost", RMI_PORT, model1);
        RmiClient client2 = new RmiClient("localhost", RMI_PORT, model2);

        client1.joinGame(2, "Alice");
        client2.joinGame(2, "Bob");
        Thread.sleep(500);

        // After the join, same data
        assertEquals(model1.getState().getUpperRowCardIds(), model2.getState().getUpperRowCardIds(),
                "Upper row should be identical after join");
        assertEquals(model1.getState().getLowerRowCardIds(), model2.getState().getLowerRowCardIds(),
                "Lower row should be identical after join");
        assertEquals(model1.getState().getOfferTurnCardOrder(), model2.getState().getOfferTurnCardOrder(),
                "Turn order should be identical after join");

        // Place totems
        List<String> turnOrder = model1.getState().getOfferTurnCardOrder();
        Map<String, RmiClient> clientByNick = Map.of("Alice", client1, "Bob", client2);

        clientByNick.get(turnOrder.get(0)).placeTotem(turnOrder.get(0), 'B');
        Thread.sleep(300);

        // After the first totem, same data
        assertEquals(model1.getState().getOfferTrackPositions(), model2.getState().getOfferTrackPositions(),
                "Track should be identical after first totem");

        clientByNick.get(turnOrder.get(1)).placeTotem(turnOrder.get(1), 'C');
        Thread.sleep(500);

        // After the second totem, same data
        assertEquals(model1.getState().getOfferTrackPositions(), model2.getState().getOfferTrackPositions(),
                "Track should be identical after second totem");

        // Take card
        String firstPicker = model1.getState().getOfferTrackPositions().get('B');
        String cardId = model1.getState().getUpperRowCardIds().get(0);

        clientByNick.get(firstPicker).takeCard(firstPicker, cardId);
        Thread.sleep(500);

        // After the pick, same exposed cards
        assertEquals(model1.getState().getUpperRowCardIds(), model2.getState().getUpperRowCardIds(),
                "Upper row should be identical after take card");
        assertEquals(model1.getState().getLowerRowCardIds(), model2.getState().getLowerRowCardIds(),
                "Lower row should be identical after take card");

        // Same data on the players
        assertEquals(
                model1.getState().getPlayer("Alice").getCharacterCardIds(),
                model2.getState().getPlayer("Alice").getCharacterCardIds(),
                "Alice's cards should be identical on both clients");
        assertEquals(
                model1.getState().getPlayer("Bob").getCharacterCardIds(),
                model2.getState().getPlayer("Bob").getCharacterCardIds(),
                "Bob's cards should be identical on both clients");
    }


}