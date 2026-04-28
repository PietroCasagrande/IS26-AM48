package it.polimi.ingsw.am48.network.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocketClientHandlerTest {

    @Mock private Socket socket;
    @Mock private GameController controller;
    @Mock private MesosServer server;
    @Mock private GameSnapshot snapshotMock;
    @Mock private JoinResult joinResultMock;
    @Mock private GameDelta deltaMock;

    private ByteArrayOutputStream outStream;
    private SocketClientHandler handler;

    private void setupHandlerWithInput(String jsonInput) throws IOException {
        outStream = new ByteArrayOutputStream();
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream((jsonInput + "\n").getBytes()));
        when(socket.getOutputStream()).thenReturn(outStream);
        handler = new SocketClientHandler(socket, controller, server);
    }

    // --- joinGame ---

    @Test
    @DisplayName("joinGame: when game is not full should send initialSnapshot to client only")
    void shouldSendSnapshotToClientWhenGameNotFull() throws Exception {
        String input = "{\"type\":\"joinGame\",\"numPlayers\":3,\"nickname\":\"P1\"}";
        setupHandlerWithInput(input);
        when(joinResultMock.snapshot()).thenReturn(snapshotMock);
        when(joinResultMock.gameStarted()).thenReturn(false);
        when(controller.handleJoinGame(3, "P1")).thenReturn(joinResultMock);

        handler.run();

        verify(server).registerClient(eq("P1"), eq(handler));
        verify(server, never()).broadcastSnapshotToGame(any(), any());
        assertTrue(outStream.toString().contains("initialSnapshot"));
    }

    @Test
    @DisplayName("joinGame: when game is full should broadcast snapshot to entire game")
    void shouldBroadcastSnapshotWhenGameFull() throws Exception {
        String input = "{\"type\":\"joinGame\",\"numPlayers\":3,\"nickname\":\"P1\"}";
        setupHandlerWithInput(input);
        when(joinResultMock.snapshot()).thenReturn(snapshotMock);
        when(joinResultMock.gameStarted()).thenReturn(true);
        when(controller.handleJoinGame(3, "P1")).thenReturn(joinResultMock);
        when(controller.getPlayersInGame("P1")).thenReturn(List.of("P1", "P2", "P3"));

        handler.run();

        verify(server).registerClient(eq("P1"), eq(handler));
        verify(server).broadcastSnapshotToGame(eq(List.of("P1", "P2", "P3")), eq(snapshotMock));
    }

    // --- placeTotem ---

    @Test
    @DisplayName("placeTotem: should broadcast all deltas to game")
    void shouldBroadcastDeltaOnPlaceTotem() throws Exception {
        String input = "{\"type\":\"joinGame\",\"numPlayers\":3,\"nickname\":\"P1\"}\n" +
                "{\"type\":\"placeTotem\",\"position\":\"A\"}";
        setupHandlerWithInput(input);
        when(joinResultMock.snapshot()).thenReturn(snapshotMock);
        when(joinResultMock.gameStarted()).thenReturn(false);
        when(controller.handleJoinGame(3, "P1")).thenReturn(joinResultMock);
        when(controller.handlePlaceTotem("P1", 'A')).thenReturn(List.of(deltaMock, deltaMock));
        when(controller.getPlayersInGame("P1")).thenReturn(List.of("P1"));

        handler.run();

        verify(server, times(2)).broadcastToGame(eq(List.of("P1")), eq(deltaMock));
    }

    // --- takeCard ---

    @Test
    @DisplayName("takeCard: should broadcast multiple deltas to game")
    void shouldBroadcastMultipleDeltasOnTakeCard() throws Exception {
        String input = "{\"type\":\"joinGame\",\"numPlayers\":3,\"nickname\":\"P1\"}\n" +
                "{\"type\":\"takeCard\",\"cardId\":\"C123\"}";
        setupHandlerWithInput(input);
        when(joinResultMock.snapshot()).thenReturn(snapshotMock);
        when(joinResultMock.gameStarted()).thenReturn(false);
        when(controller.handleJoinGame(3, "P1")).thenReturn(joinResultMock);
        when(controller.handleTakeCard("P1", "C123")).thenReturn(List.of(deltaMock, deltaMock));
        when(controller.getPlayersInGame("P1")).thenReturn(List.of("P1"));

        handler.run();

        verify(server, times(2)).broadcastToGame(eq(List.of("P1")), eq(deltaMock));
    }

    // --- error handling ---

    @Test
    @DisplayName("exception in controller should send error to client only")
    void shouldSendErrorToClientOnControllerException() throws Exception {
        String input = "{\"type\":\"joinGame\",\"numPlayers\":3,\"nickname\":\"P1\"}\n" +
                "{\"type\":\"placeTotem\",\"position\":\"X\"}";
        setupHandlerWithInput(input);
        when(joinResultMock.snapshot()).thenReturn(snapshotMock);
        when(joinResultMock.gameStarted()).thenReturn(false);
        when(controller.handleJoinGame(3, "P1")).thenReturn(joinResultMock);
        when(controller.handlePlaceTotem("P1", 'X')).thenThrow(new IllegalStateException("Invalid move"));

        handler.run();

        assertTrue(outStream.toString().contains("error"));
        assertTrue(outStream.toString().contains("Invalid move"));
        verify(server, never()).broadcastToGame(any(), any());
    }

    // --- connection ---

    @Test
    @DisplayName("run: loop terminates and closes socket on EOF")
    void shouldCloseSocketOnEOF() throws Exception {
        setupHandlerWithInput("");
        handler.run();
        verify(socket).close();
    }
}