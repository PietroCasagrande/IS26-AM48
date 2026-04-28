package it.polimi.ingsw.am48.network.messages.commands;

import it.polimi.ingsw.am48.controller.GameController;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.server.MesosServer;
import it.polimi.ingsw.am48.network.server.SocketClientHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandExecutionTest {

    @Mock private GameController controller;
    @Mock private MesosServer server;
    @Mock private SocketClientHandler handler;
    @Mock private VirtualView selfView;
    @Mock private JoinResult joinResult;
    @Mock private GameDelta delta;

    @Test
    @DisplayName("JoinGameCommand should send snapshot to client when game does not start")
    void shouldSendSnapshotToClientWhenGameDoesNotStart() throws Exception {
        when(handler.getSelfView()).thenReturn(selfView);
        when(handler.getServer()).thenReturn(server);
        when(handler.getController()).thenReturn(controller);
        when(controller.handleJoinGame(3, "Alice")).thenReturn(joinResult);
        when(joinResult.gameStarted()).thenReturn(false);

        JoinGameCommand command = new JoinGameCommand(3, "Alice");
        command.execute(handler);

        verify(server).registerClient(eq("Alice"), eq(selfView));
        verify(selfView).showInitialSnapshot(eq(joinResult.snapshot()));
        verify(server, never()).broadcastSnapshotToGame(any(), any());
    }

    @Test
    @DisplayName("PlaceTotemCommand should broadcast delta to all game clients")
    void shouldBroadcastDeltasForPlaceTotemCommand() {
        when(handler.getServer()).thenReturn(server);
        when(handler.getController()).thenReturn(controller);
        when(handler.getNickname()).thenReturn("Alice");
        when(controller.handlePlaceTotem("Alice", 'A')).thenReturn(List.of(delta, delta));
        when(controller.getPlayersInGame("Alice")).thenReturn(List.of("Alice", "Bob"));

        PlaceTotemCommand command = new PlaceTotemCommand('A');
        command.execute(handler);

        verify(server, times(2)).broadcastToGame(eq(List.of("Alice", "Bob")), eq(delta));
    }

    @Test
    @DisplayName("PlaceTotemCommand should notify the sender on exception")
    void shouldNotifySenderOnPlaceTotemError() throws Exception {
        when(handler.getSelfView()).thenReturn(selfView);
        when(handler.getController()).thenReturn(controller);
        when(handler.getNickname()).thenReturn("Alice");
        when(controller.handlePlaceTotem("Alice", 'Z')).thenThrow(new IllegalStateException("Invalid move"));

        PlaceTotemCommand command = new PlaceTotemCommand('Z');
        command.execute(handler);

        verify(selfView).reportError("Invalid move");
        verify(server, never()).broadcastToGame(any(), any());
    }
}
