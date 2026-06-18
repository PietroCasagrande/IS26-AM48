package it.polimi.ingsw.am48.network.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.delta.TotemPlacedDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.messages.commands.*;
import it.polimi.ingsw.am48.network.messages.notifications.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    // --- ClientCommand ---

    @Test
    @DisplayName("JoinGameCommand should survive JSON round-trip")
    void shouldSerializeAndDeserializeJoinGame() throws Exception {
        JoinGameCommand original = new JoinGameCommand(3, "Pietro");
        String json = mapper.writeValueAsString(original);
        ClientCommand deserialized = mapper.readValue(json, ClientCommand.class);

        assertTrue(deserialized instanceof JoinGameCommand);
        assertTrue(json.contains("\"type\":\"joinGame\""));
        assertTrue(json.contains("\"numPlayers\":3"));
        assertTrue(json.contains("\"nickname\":\"Pietro\""));
    }

    @Test
    @DisplayName("PlaceTotemCommand should survive JSON round-trip")
    void shouldSerializeAndDeserializePlaceTotem() throws Exception {
        PlaceTotemCommand original = new PlaceTotemCommand('A');
        String json = mapper.writeValueAsString(original);
        ClientCommand deserialized = mapper.readValue(json, ClientCommand.class);

        assertTrue(deserialized instanceof PlaceTotemCommand);
        assertTrue(json.contains("\"type\":\"placeTotem\""));
    }

    @Test
    @DisplayName("TakeCardCommand should survive JSON round-trip")
    void shouldSerializeAndDeserializeTakeCard() throws Exception {
        TakeCardCommand original = new TakeCardCommand("char_01");
        String json = mapper.writeValueAsString(original);
        ClientCommand deserialized = mapper.readValue(json, ClientCommand.class);

        assertTrue(deserialized instanceof TakeCardCommand);
        assertTrue(json.contains("\"type\":\"takeCard\""));
        assertTrue(json.contains("\"cardId\":\"char_01\""));
    }

    @Test
    @DisplayName("Unknown command type should throw on deserialization")
    void shouldThrowOnUnknownCommandType() {
        String json = "{\"type\":\"unknownCommand\",\"data\":123}";
        assertThrows(Exception.class, () -> mapper.readValue(json, ClientCommand.class));
    }

    // --- ServerNotification ---

    @Test
    @DisplayName("ErrorNotification should survive JSON round-trip")
    void shouldSerializeAndDeserializeError() throws Exception {
        ErrorNotification original = new ErrorNotification("Not your turn");
        String json = mapper.writeValueAsString(original);
        ServerNotification deserialized = mapper.readValue(json, ServerNotification.class);

        assertTrue(deserialized instanceof ErrorNotification);
        assertTrue(json.contains("\"type\":\"error\""));
        assertTrue(json.contains("\"message\":\"Not your turn\""));
    }

    @Test
    @DisplayName("GameDeltaNotification should survive JSON round-trip with polymorphic delta")
    void shouldSerializeAndDeserializeGameDelta() throws Exception {
        TotemPlacedDelta delta = new TotemPlacedDelta("Pietro", 'A', List.of("Bob"), "PlaceTotem");
        GameDeltaNotification original = new GameDeltaNotification(delta);
        String json = mapper.writeValueAsString(original);
        ServerNotification deserialized = mapper.readValue(json, ServerNotification.class);

        assertTrue(deserialized instanceof GameDeltaNotification);
        assertTrue(json.contains("\"type\":\"gameDelta\""));
    }

    @Test
    @DisplayName("InitialSnapshotNotification should serialize with correct type")
    void shouldSerializeInitialSnapshot() throws Exception {
        // Non possiamo costruire un GameSnapshot reale facilmente,
        // ma possiamo verificare che la serializzazione non crashi
        // e che il type sia corretto
        InitialSnapshotNotification original = new InitialSnapshotNotification(null);
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"type\":\"initialSnapshot\""));
    }

    @Test
    @DisplayName("Unknown notification type should throw on deserialization")
    void shouldThrowOnUnknownNotificationType() {
        String json = "{\"type\":\"unknownNotification\",\"data\":123}";
        assertThrows(Exception.class, () -> mapper.readValue(json, ServerNotification.class));
    }
}