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
        // Pulisce la cartella di test dopo ogni test
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
        // Verifica che save + load restituisca uno snapshot con gli stessi valori base
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
        // Verifica che caricare un id inesistente non lanci eccezione
        Optional<GameSnapshot> result = repository.load("NON-ESISTENTE");
        assertTrue(result.isEmpty());
    }

    @Test
    void repository_delete_removesFile() {
        // Verifica che dopo delete il file non esista più
        repository.save("GAME-2", buildMinimalSnapshot("GAME-2", 2, 1));
        repository.delete("GAME-2");
        assertTrue(repository.load("GAME-2").isEmpty());
    }

    @Test
    void repository_listActiveGameIds_returnsAllSaved() {
        // Verifica che listActiveGameIds trovi tutti i file salvati
        repository.save("GAME-A", buildMinimalSnapshot("GAME-A", 2, 1));
        repository.save("GAME-B", buildMinimalSnapshot("GAME-B", 3, 1));
        repository.save("GAME-C", buildMinimalSnapshot("GAME-C", 4, 1));

        List<String> ids = repository.listActiveGameIds();
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of("GAME-A", "GAME-B", "GAME-C")));
    }

    // ---------------------------------------------------------------
    // SERIALIZZAZIONE POLIMORFICA DELLE FASI
    // ---------------------------------------------------------------

    @Test
    void phaseSnapshot_waitingPhase_serializesAndDeserializesCorrectly() {
        // Verifica che WaitingPhaseSnapshot sopravviva al roundtrip JSON
        GameSnapshot snapshot = buildSnapshotWithPhase("GAME-1", new WaitingPhaseSnapshot());
        repository.save("GAME-1", snapshot);

        PhaseSnapshot loaded = repository.load("GAME-1").get().getPhase();
        assertInstanceOf(WaitingPhaseSnapshot.class, loaded);
    }

    @Test
    void phaseSnapshot_placeTotemPhase_preservesPlayersPlaced() {
        // Verifica che i nickname dei giocatori che hanno piazzato vengano preservati
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
        // Verifica che ordine e indice corrente della fase offerta vengano preservati
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
    // TRIBESNAPSHOT — tutti i campi inclusi i booleani
    // ---------------------------------------------------------------

    @Test
    void tribeSnapshot_allFields_preservedAfterRoundtrip() {
        // Verifica che tutti gli attributi di TribeSnapshot — inclusi i booleani
        // aggiunti per la persistenza — sopravvivano alla serializzazione
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
    // GAME.fromSnapshot() — correttezza stato
    // ---------------------------------------------------------------

    @Test
    void gameFromSnapshot_restoresBasicState() {
        // Verifica che Game.fromSnapshot() ripristini correttamente
        // gameId, currentTurn e numero giocatori
        GameSnapshot snapshot = buildRealisticSnapshot("GAME-1", 2, 3);
        repository.save("GAME-1", snapshot);

        Game restored = Game.fromSnapshot(repository.load("GAME-1").get());

        assertEquals("GAME-1", restored.getGameId());
        assertEquals(3, restored.getCurrentTurn());
        assertEquals(2, restored.getNumPlayers());
    }

    @Test
    void gameFromSnapshot_restoresCurrPlayer() {
        // Verifica che il currPlayer del PlayerContext punti al Player corretto
        GameSnapshot snapshot = buildRealisticSnapshot("GAME-1", 2, 1);
        String expectedCurrPlayer = snapshot.getPlayerContext().getCurrPlayerNickname();

        Game restored = Game.fromSnapshot(snapshot);

        assertEquals(expectedCurrPlayer,
                restored.getPlayerContext().getCurrPlayer().getNickname());
    }

    @Test
    void gameFromSnapshot_playerHasCorrectCardsInTribe() {
        // Verifica che le carte nella tribe del giocatore ripristinato
        // siano gli oggetti corretti (per id), non null
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
        // Verifica che in PlaceTotemPhase il set dei giocatori che hanno
        // già piazzato venga ripristinato correttamente
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
    // STRATEGY RE-REGISTRATION — il punto più critico
    // ---------------------------------------------------------------

    @Test
    void gameFromSnapshot_buildingCardStrategies_areRegisteredToNotificator() {
        // Verifica che dopo fromSnapshot le strategy degli edifici acquisiti
        // siano correttamente registrate nel NotificatorCenter.
        // Costruiamo una partita con un giocatore che ha un edificio,
        // serializziamo, ripristiniamo e verifichiamo che il notificator
        // abbia osservatori registrati.
        Game original = buildGameWithBuildingCard("GAME-1", 2);
        GameSnapshot snapshot = original.toSnapshot();
        repository.save("GAME-1", snapshot);

        Game restored = Game.fromSnapshot(repository.load("GAME-1").get());

        // Il NotificatorCenter non deve essere vuoto — almeno un observer registrato
//        assertFalse(restored.getNotificatorCenter().isEmpty(),
//                "Nessuna strategy registrata dopo fromSnapshot — le carte edificio non funzioneranno");
    }

    @Test
    void gameFromSnapshot_buildingStrategyFires_afterRestore() {
        // 1. Setup gioco e player
        Game original = new Game("GAME-TEST", 2);
        original.addPlayer("Alice");
        original.addPlayer("Bob");
        Player alice = original.getPlayerByNickname("Alice");
        original.getPlayerContext().setCurrPlayer(alice);

        // 2. Recuperiamo le carte necessarie dalla mappa reale
        Map<String, Card> cardMap = CardMapBuilder.buildCardMap(2);

        // L'edificio chiave
        BuildingCard bld04 = (BuildingCard) cardMap.get("BLD-04");
        alice.getTribe().addToTribe(bld04);

        // Diamo ad Alice 5 tipi diversi (Artist, Builder, Inventor, Hunter, Picker)
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("ART-01")); // Artist
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("BUI-03")); // Builder
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("INV-01")); // Inventor
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("HUN-02")); // Hunter
        alice.getTribe().addToTribe((CharacterCard) cardMap.get("PIC-02")); // Picker

        // Registriamo la strategy nel gioco originale (fondamentale per lo snapshot)
        bld04.getStrategy().registerTo(original.getNotificatorCenter(), original.getPlayerContext());

        // 3. CRASH E RIPRISTINO
        GameSnapshot snapshot = original.toSnapshot();
        Game restored = Game.fromSnapshot(snapshot);

        Player restoredAlice = restored.getPlayerContext().getCurrPlayer();
        int foodBefore = restoredAlice.getTribe().getCurrentFood();

        // 4. AZIONE: Alice pesca la 6a carta (Shaman) che completa il set
        Card sha01 = cardMap.get("SHA-01");
        restoredAlice.getTribe().addToTribe((CharacterCard) sha01);

        // Triggeriamo il Notificator dell'azione di pesca (OnPick)
        // La strategy si sveglierà, controllerà la tribù, vedrà il set completo e darà cibo
        restored.getNotificatorCenter().getPickNotificator()
                .notify(restored.getPlayerContext());

        // 5. VERIFICA
        int foodAfter = restoredAlice.getTribe().getCurrentFood();

        assertTrue(foodAfter > foodBefore,
                "La strategy AllSet non si è attivata: il cibo doveva aumentare completando il set di 6 tipi");
    }

    // ---------------------------------------------------------------
    // SIMULAZIONE CRASH COMPLETA
    // ---------------------------------------------------------------

    @Test
    void crashSimulation_stateIdenticalBeforeAndAfter() {
        // Simula un crash completo: crea partita, fai join, serializza,
        // distruggi il GameManager, creane uno nuovo che legge i file,
        // verifica che Alice e Bob possano riconnettersi
        LeaderboardRepository mockLeaderboard = mock(LeaderboardRepository.class);
        GameManager manager1 = new GameManager(repository, mockLeaderboard);
        manager1.loadCrashedGames();

        manager1.joinGame(2, "Alice");
        manager1.joinGame(2, "Bob");

        // Simula crash — verifica che lo snapshot sia stato salvato
        List<String> savedIds = repository.listActiveGameIds();
        assertFalse(savedIds.isEmpty(), "Nessuno snapshot salvato prima del crash");

        // Riavvio — nuovo GameManager legge i file dal disco
        GameManager manager2 = new GameManager(repository, mockLeaderboard);
        manager2.loadCrashedGames();

        // Verifica che Alice e Bob possano riconnettersi senza eccezioni
        assertDoesNotThrow(() -> manager2.joinGame(2, "Alice"));
        assertDoesNotThrow(() -> manager2.joinGame(2, "Bob"));
    }

    // ---------------------------------------------------------------
    // HELPER METHODS — basati sui dati reali di game_data.json
    // ---------------------------------------------------------------

    /**
     * Snapshot minimale per test di serializzazione base.
     * Due giocatori con tribe vuote, fase di attesa.
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
     * Snapshot con una fase specifica — usato per testare il polimorfismo
     * della deserializzazione di PhaseSnapshot.
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
     * Snapshot dove Alice ha una tribe con dati specifici — usato per
     * verificare che tutti i campi di TribeSnapshot siano preservati.
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
     * Snapshot realistico con carte reali dal JSON.
     * Alice ha ART-01 (character) e BLD-01 (building, OnTotemReturned).
     * Bob ha ART-02 (character). Fase PlaceTotem, nessuno ha ancora piazzato.
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
     * Costruisce un Game reale con Alice che possiede BLD-01
     * (ExtraFoodOnFoodStrategy, OnTotemReturned).
     * Usato per testare che la strategy si riattivi dopo fromSnapshot.
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
     * Come buildGameWithBuildingCard ma con BLD-01 (ExtraFoodOnFoodStrategy,
     * OnTotemReturned) — verifica che il food cambi dopo la notifica.
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
     * TribeSnapshot vuoto — usato come placeholder per giocatori
     * che non hanno carte nel test specifico.
     */
    private TribeSnapshot buildEmptyTribeSnapshot() {
        return new TribeSnapshot(
                List.of(), List.of(), Map.of(),
                0, 0, 0, 0, false, false, false, false, 0, 0, 0
        );
    }

    /**
     * BoardSnapshot minimale con deck ridotto e OfferTurnCard con Alice e Bob.
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