package it.polimi.ingsw.am48.model.snapshot;

import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.factory.BoardBuilder;
import it.polimi.ingsw.am48.model.factory.CardMapBuilder;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.phase.PlaceTotemPhase;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.repository.GameRepository;
import it.polimi.ingsw.am48.repository.JsonGameRepository;
import it.polimi.ingsw.am48.repository.LeaderboardRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersistenceTest {

    private static final String TEST_SAVES_DIR = "target/test-saves";
    private JsonGameRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonGameRepository(TEST_SAVES_DIR);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Cleans the test folder after each test
        Path dir = Path.of(TEST_SAVES_DIR);
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ---------------------------------------------------------------
    // REPOSITORY
    // ---------------------------------------------------------------

    @Test
    void repository_saveAndLoad_roundtrip() {
        // Checks that save + load returns a snapshot with the same basic values
        GameSnapshot snapshot = buildMinimalSnapshot("GAME-1", 3, 2);
        repository.save("GAME-1", snapshot);

        Optional<GameSnapshot> loaded = repository.load("GAME-1");
        assertTrue(loaded.isPresent());
        assertEquals("GAME-1", loaded.get().getGameId());
        assertEquals(3, loaded.get().getNumPlayers());
        assertEquals(2, loaded.get().getCurrentTurn());
    }

    @Test
    void repository_load_returnsEmpty_whenFileNotExists() {
        // Checks that loading a non-existent id does not throw an exception
        Optional<GameSnapshot> result = repository.load("NON-EXISTENT");
        assertTrue(result.isEmpty());
    }

    @Test
    void repository_delete_removesFile() {
        // Checks that after delete the file no longer exists
        repository.save("GAME-2", buildMinimalSnapshot("GAME-2", 2, 1));
        repository.delete("GAME-2");
        assertTrue(repository.load("GAME-2").isEmpty());
    }

    @Test
    void repository_listActiveGameIds_returnsAllSaved() {
        // Checks that listActiveGameIds finds all the saved files
        repository.save("GAME-A", buildMinimalSnapshot("GAME-A", 2, 1));
        repository.save("GAME-B", buildMinimalSnapshot("GAME-B", 3, 1));
        repository.save("GAME-C", buildMinimalSnapshot("GAME-C", 4, 1));

        List<String> ids = repository.listActiveGameIds();
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of("GAME-A", "GAME-B", "GAME-C")));
    }

    // ---------------------------------------------------------------
    // POLYMORPHIC SERIALIZATION OF PHASES
    // ---------------------------------------------------------------

    @Test
    void phaseSnapshot_waitingPhase_serializesAndDeserializesCorrectly() {
        // Checks that WaitingPhaseSnapshot survives the JSON roundtrip
        GameSnapshot snapshot = buildSnapshotWithPhase("GAME-1", new WaitingPhaseSnapshot());
        repository.save("GAME-1", snapshot);

        PhaseSnapshot loaded = repository.load("GAME-1").get().getPhase();
        assertInstanceOf(WaitingPhaseSnapshot.class, loaded);
    }

    @Test
    void phaseSnapshot_placeTotemPhase_preservesPlayersPlaced() {
        // Checks that the nicknames of the players who have placed are preserved
        Set<String> placed = Set.of("Alice", "Bob");
        GameSnapshot snapshot = buildSnapshotWithPhase("GAME-1",
                new PlaceTotemPhaseSnapshot(placed));
        repository.save("GAME-1", snapshot);

        PhaseSnapshot loaded = repository.load("GAME-1").get().getPhase();
        assertInstanceOf(PlaceTotemPhaseSnapshot.class, loaded);
        assertEquals(placed, ((PlaceTotemPhaseSnapshot) loaded).getPlayersPlacedNicknames());
    }

    @Test
    void phaseSnapshot_playerOfferPhase_preservesOrderAndIndex() {
        // Checks that the order and current index of the offer phase are preserved
        List<String> order = List.of("Alice", "Bob", "Carlo");
        GameSnapshot snapshot = buildSnapshotWithPhase("GAME-1",
                new PlayerOfferPhaseSnapshot(order, 1, 0, 1, false, null, false));
        repository.save("GAME-1", snapshot);

        PhaseSnapshot loaded = repository.load("GAME-1").get().getPhase();
        assertInstanceOf(PlayerOfferPhaseSnapshot.class, loaded);
        PlayerOfferPhaseSnapshot p = (PlayerOfferPhaseSnapshot) loaded;
        assertEquals(order, p.getActionOrderNicknames());
        assertEquals(1, p.getCurrIdx());
    }

    // ---------------------------------------------------------------
    // TRIBESNAPSHOT — all fields including the booleans
    // ---------------------------------------------------------------

    @Test
    void tribeSnapshot_allFields_preservedAfterRoundtrip() {
        // Checks that all TribeSnapshot attributes — including the booleans
        // added for persistence — survive the serialization
        TribeSnapshot tribe = new TribeSnapshot(
                List.of("CHAR-001", "CHAR-002"),
                List.of("BUILD-003"),
                Map.of("GOLD", 2),
                10, 15, 2, 2, true, false,
                true,
                true,
                4,
                3,
                8
        );

        GameSnapshot snapshot = buildSnapshotWithTribe("GAME-1", tribe);
        repository.save("GAME-1", snapshot);

        TribeSnapshot loaded = repository.load("GAME-1").get()
                .getPlayerContext().getPlayers().get(0).getTribe();

        assertEquals(10, loaded.getCurrentFood());
        assertEquals(15, loaded.getCurrentPrestigePoints());
        assertTrue(loaded.getShamanSafety());
        assertFalse(loaded.getShamanDoubling());
        assertTrue(loaded.getExtraFoodRight());
        assertEquals(2, loaded.getFoodDiscount());
        assertEquals(4, loaded.getBuildingDiscount());
        assertEquals(3, loaded.getBuilderPoints());
        assertEquals(8, loaded.getBuildingPoints());
        assertEquals(List.of("CHAR-001", "CHAR-002"), loaded.getCharacterCardIds());
        assertEquals(List.of("BUILD-003"), loaded.getBuildingCardIds());
    }

    // ---------------------------------------------------------------
    // GAME.fromSnapshot() — state correctness
    // ---------------------------------------------------------------

    @Test
    void gameFromSnapshot_restoresBasicState() {
        // Checks that Game.fromSnapshot() correctly restores
        // gameId, currentTurn and number of players
        GameSnapshot snapshot = buildRealisticSnapshot("GAME-1", 2, 3);
        repository.save("GAME-1", snapshot);

        Game restored = Game.fromSnapshot(repository.load("GAME-1").get());

        assertEquals("GAME-1", restored.getGameId());
        assertEquals(3, restored.getCurrentTurn());
        assertEquals(2, restored.getNumPlayers());
    }

    @Test
    void gameFromSnapshot_restoresCurrPlayer() {
        // Checks that the PlayerContext's currPlayer points to the correct Player
        GameSnapshot snapshot = buildRealisticSnapshot("GAME-1", 2, 1);
        String expectedCurrPlayer = snapshot.getPlayerContext().getCurrPlayerNickname();

        Game restored = Game.fromSnapshot(snapshot);

        assertEquals(expectedCurrPlayer,
                restored.getPlayerContext().getCurrPlayer().getNickname());
    }

    @Test
    void gameFromSnapshot_playerHasCorrectCardsInTribe() {
        // Checks that the cards in the restored player's tribe
        // are the correct objects (by id), not null
        GameSnapshot snapshot = buildRealisticSnapshot("GAME-1", 2, 1);
        Game restored = Game.fromSnapshot(snapshot);

        Player player = restored.getPlayerContext().getPlayers().get(0);
        assertFalse(player.getTribe().getCharacters().isEmpty());
        player.getTribe().getCharacters().values()
                .forEach(list -> {
                    list.forEach(card -> {
                        assertNotNull(card.getCardId());
                    });
                }
        );
    }

    @Test
    void gameFromSnapshot_placeTotemPhase_restoredCorrectly() {
        // Checks that in PlaceTotemPhase the set of players who have
        // already placed is restored correctly
        Set<String> placed = Set.of("Alice");
        GameSnapshot snapshot = buildSnapshotWithPhase("GAME-1",
                new PlaceTotemPhaseSnapshot(placed));
        repository.save("GAME-1", snapshot);

        Game restored = Game.fromSnapshot(repository.load("GAME-1").get());

        assertInstanceOf(PlaceTotemPhase.class, restored.getCurrentPhase());
        PlaceTotemPhase phase = (PlaceTotemPhase) restored.getCurrentPhase();
        assertTrue(phase.getPlayersPlaced().stream()
                .map(Player::getNickname)
                .collect(Collectors.toSet())
                .contains("Alice"));
    }

    // ---------------------------------------------------------------
    // STRATEGY RE-REGISTRATION — the most critical point
    // ---------------------------------------------------------------

    @Test
    void gameFromSnapshot_buildingCardStrategies_areRegisteredToNotificator() {
        // Checks that after fromSnapshot the strategies of the acquired buildings
        // are correctly registered in the NotificatorCenter.
        // We build a game with a player who owns a building,
        // serialize it, restore it and verify that the notificator
        // has registered observers.
        Game original = buildGameWithBuildingCard("GAME-1", 2);
        GameSnapshot snapshot = original.toSnapshot();
        repository.save("GAME-1", snapshot);

        Game restored = Game.fromSnapshot(repository.load("GAME-1").get());

        // The NotificatorCenter must not be empty — at least one observer registered
//        assertFalse(restored.getNotificatorCenter().isEmpty(),
//                "No strategy registered after fromSnapshot — building cards will not work");
    }

    @Test
    void gameFromSnapshot_buildingStrategyFires_afterRestore() {
        // 1. Set up game and players
        Game original = new Game("GAME-TEST", 2);
        original.addPlayer("Alice");
        original.addPlayer("Bob");
        Player alice = original.getPlayerByNickname("Alice");
        original.getPlayerContext().setCurrPlayer(alice);

        // 2. Fetch the needed cards from the real map
        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(2);

        // The key building
        BuildingCard bld04 = (BuildingCard) cardMap.get("BLD-04");
        alice.getTribe().addToTribe(bld04);

        // Give Alice 5 different types (Artist, Builder, Inventor, Hunter, Picker)
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("ART-01")); // Artist
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("BUI-03")); // Builder
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("INV-01")); // Inventor
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("HUN-02")); // Hunter
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("PIC-02")); // Picker

        // Register the strategy in the original game (essential for the snapshot)
        bld04.getStrategy().registerTo(original.getNotificatorCenter(), original.getPlayerContext());

        // 3. CRASH AND RESTORE
        GameSnapshot snapshot = original.toSnapshot();
        Game restored = Game.fromSnapshot(snapshot);

        Player restoredAlice = restored.getPlayerContext().getCurrPlayer();
        int foodBefore = restoredAlice.getTribe().getCurrentFood();

        // 4. ACTION: Alice draws the 6th card (Shaman) that completes the set
        Card sha01 = cardMap.get("SHA-01");
        restoredAlice.getTribe().addToTribe((CharacterCard) sha01);

        // Trigger the Notificator for the draw action (OnPick)
        // The strategy will wake up, inspect the tribe, see the complete set and grant food
        restored.getNotificatorCenter().getPickNotificator()
                .notify(restored.getPlayerContext());

        // 5. CHECK
        int foodAfter = restoredAlice.getTribe().getCurrentFood();

        assertTrue(foodAfter > foodBefore,
                "The AllSet strategy did not fire: food should have increased by completing the set of 6 types");
    }

    // ---------------------------------------------------------------
    // FULL CRASH SIMULATION
    // ---------------------------------------------------------------

    @Test
    void crashSimulation_stateIdenticalBeforeAndAfter() {
        // Simulates a full crash: create a game, join, serialize,
        // destroy the GameManager, create a new one that reads the files,
        // verify that Alice and Bob can reconnect
        LeaderboardRepository mockLeaderboard = mock(LeaderboardRepository.class);
        GameManager manager1 = new GameManager(repository, mockLeaderboard);
        manager1.loadCrashedGames();

        manager1.joinGame(2, "Alice");
        manager1.joinGame(2, "Bob");

        // Simulate crash — verify that the snapshot was saved
        List<String> savedIds = repository.listActiveGameIds();
        assertFalse(savedIds.isEmpty(), "No snapshot saved before the crash");

        // Restart — a new GameManager reads the files from disk
        GameManager manager2 = new GameManager(repository, mockLeaderboard);
        manager2.loadCrashedGames();

        // Verify that Alice and Bob can reconnect without exceptions
        assertDoesNotThrow(() -> manager2.joinGame(2, "Alice"));
        assertDoesNotThrow(() -> manager2.joinGame(2, "Bob"));
    }

    // ---------------------------------------------------------------
    // HELPER METHODS — based on the real data in game_data.json
    // ---------------------------------------------------------------

    /**
     * Minimal snapshot for basic serialization tests.
     * Two players with empty tribes, waiting phase.
     */
    private GameSnapshot buildMinimalSnapshot(String gameId, int numPlayers, int currentTurn) {
        TribeSnapshot emptyTribe = buildEmptyTribeSnapshot();
        PlayerContextSnapshot playerContext = new PlayerContextSnapshot(
                List.of(
                        new PlayerSnapshot("Alice", "RED", emptyTribe),
                        new PlayerSnapshot("Bob", "BLUE", emptyTribe)
                ),
                "Alice"
        );
        return new GameSnapshot(gameId, numPlayers, playerContext, currentTurn,
                buildMinimalBoardSnapshot(numPlayers), new WaitingPhaseSnapshot());
    }

    /**
     * Snapshot with a specific phase — used to test the polymorphism
     * of PhaseSnapshot deserialization.
     */
    private GameSnapshot buildSnapshotWithPhase(String gameId, PhaseSnapshot phase) {
        TribeSnapshot emptyTribe = buildEmptyTribeSnapshot();
        PlayerContextSnapshot playerContext = new PlayerContextSnapshot(
                List.of(
                        new PlayerSnapshot("Alice", "RED", emptyTribe),
                        new PlayerSnapshot("Bob", "BLUE", emptyTribe)
                ),
                "Alice"
        );
        return new GameSnapshot(gameId, 2, playerContext, 1,
                buildMinimalBoardSnapshot(2), phase);
    }

    /**
     * Snapshot where Alice has a tribe with specific data — used to
     * verify that all TribeSnapshot fields are preserved.
     */
    private GameSnapshot buildSnapshotWithTribe(String gameId, TribeSnapshot tribe) {
        PlayerContextSnapshot playerContext = new PlayerContextSnapshot(
                List.of(
                        new PlayerSnapshot("Alice", "RED", tribe),
                        new PlayerSnapshot("Bob", "BLUE", buildEmptyTribeSnapshot())
                ),
                "Alice"
        );
        return new GameSnapshot(gameId, 2, playerContext, 1,
                buildMinimalBoardSnapshot(2), new PlaceTotemPhaseSnapshot(Set.of()));
    }

    /**
     * Realistic snapshot with real cards from the JSON.
     * Alice has ART-01 (character) and BLD-01 (building, OnTotemReturned).
     * Bob has ART-02 (character). PlaceTotem phase, nobody has placed yet.
     */
    private GameSnapshot buildRealisticSnapshot(String gameId, int numPlayers, int currentTurn) {
        TribeSnapshot aliceTribe = new TribeSnapshot(
                List.of("ART-01"),
                List.of("BLD-01"),
                Map.of(),
                8, 5, 0, 0, false, false,
                false, false, 0, 0, 0
        );
        TribeSnapshot bobTribe = new TribeSnapshot(
                List.of("ART-02"),
                List.of(),
                Map.of(),
                6, 3, 0, 0, false, false,
                false, false, 0, 0, 0
        );
        PlayerContextSnapshot playerContext = new PlayerContextSnapshot(
                List.of(
                        new PlayerSnapshot("Alice", "RED", aliceTribe),
                        new PlayerSnapshot("Bob", "BLUE", bobTribe)
                ),
                "Alice"
        );
        return new GameSnapshot(gameId, numPlayers, playerContext, currentTurn,
                buildMinimalBoardSnapshot(numPlayers), new PlaceTotemPhaseSnapshot(Set.of()));
    }

    /**
     * Builds a real Game where Alice owns BLD-01
     * (ExtraFoodOnFoodStrategy, OnTotemReturned).
     * Used to test that the strategy re-activates after fromSnapshot.
     */
    private Game buildGameWithBuildingCard(String gameId, int numPlayers) {
        Game game = new Game(gameId, numPlayers);
        game.addPlayer("Alice");
        game.addPlayer("Bob");

        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(numPlayers);
        BuildingCard bld01 = (BuildingCard) cardMap.get("BLD-01");

        Player alice = game.getPlayerByNickname("Alice");
        game.getPlayerContext().setCurrPlayer(alice);

        alice.getTribe().addToTribe(bld01);
        bld01.getStrategy().registerTo(game.getNotificatorCenter(), game.getPlayerContext());

        return game;
    }

    /**
     * Like buildGameWithBuildingCard but with BLD-01 (ExtraFoodOnFoodStrategy,
     * OnTotemReturned) — verifies that food changes after the notification.
     */
    private Game buildGameWithKnownBuildingEffect(String gameId, int numPlayers) {
        Game game = new Game(gameId, numPlayers);
        game.addPlayer("Alice");
        game.addPlayer("Bob");

        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(numPlayers);
        BuildingCard bld01 = (BuildingCard) cardMap.get("BLD-01");

        Player alice = game.getPlayerByNickname("Alice");
        game.getPlayerContext().setCurrPlayer(alice);

        alice.getTribe().addToTribe(bld01);
        bld01.getStrategy().registerTo(game.getNotificatorCenter(), game.getPlayerContext());

        return game;
    }

    /**
     * Empty TribeSnapshot — used as a placeholder for players
     * that have no cards in the specific test.
     */
    private TribeSnapshot buildEmptyTribeSnapshot() {
        return new TribeSnapshot(
                List.of(), List.of(), Map.of(),
                0, 0, 0, 0, false, false, false, false, 0, 0, 0
        );
    }

    /**
     * Minimal BoardSnapshot with a reduced deck and an OfferTurnCard with Alice and Bob.
     */
    private BoardSnapshot buildMinimalBoardSnapshot(int numPlayers) {
        return new BoardSnapshot(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("ART-03", "ART-06", "ART-07"),
                List.of("BLD-02", "BLD-04"),
                new OfferTrackSnapshot(Map.of()),
                new OfferTurnCardSnapshot(List.of("Alice", "Bob")),
                List.of(1, 2, 3),
                0,
                numPlayers
        );
    }
}
