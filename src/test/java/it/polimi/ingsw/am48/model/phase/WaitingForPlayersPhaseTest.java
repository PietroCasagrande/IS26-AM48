package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingForPlayersPhaseTest {

    @Mock private Game game;
    @Mock private PlayerContext playerContext;

    private static final int REQUIRED = 3;
    private WaitingForPlayersPhase phase;

    @BeforeEach
    void setUp() {
        phase = new WaitingForPlayersPhase(REQUIRED);
    }

    // ==================== toSnapshot ====================

    @Test
    @DisplayName("toSnapshot: should return a WaitingPhaseSnapshot instance")
    void shouldReturnCorrectSnapshotType() {
        assertInstanceOf(WaitingPhaseSnapshot.class, phase.toSnapshot());
    }

    // ==================== addPlayer - player is NOT the last ====================

    @Nested
    @DisplayName("When the added player does NOT complete the lobby")
    class NotLastPlayer {

        @Test
        @DisplayName("should create a Player with the correct totem (index 0)")
        void shouldAssignFirstTotemToFirstPlayer() {
            when(playerContext.getPlayers()).thenReturn(new ArrayList<>());

            phase.addPlayer(playerContext, game, "Alice");

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
            verify(playerContext).addPlayer(captor.capture());
            Player added = captor.getValue();

            assertEquals("Alice", added.getNickname());
            assertEquals(Totem.values()[0], added.getTotem());
        }

        @Test
        @DisplayName("should create a Player with the correct totem (index 1)")
        void shouldAssignSecondTotemToSecondPlayer() {
            List<Player> existing = new ArrayList<>();
            existing.add(mock(Player.class));
            when(playerContext.getPlayers()).thenReturn(existing);

            phase.addPlayer(playerContext, game, "Bob");

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
            verify(playerContext).addPlayer(captor.capture());
            assertEquals(Totem.values()[1], captor.getValue().getTotem());
        }

        @Test
        @DisplayName("should not call setBoard or setPhase")
        void shouldNotTransitionPhaseOrSetBoard() {
            when(playerContext.getPlayers()).thenReturn(new ArrayList<>());

            phase.addPlayer(playerContext, game, "Alice");

            verify(game, never()).setBoard(any());
            verify(game, never()).setPhase(any());
        }
    }

    // ==================== addPlayer - player IS the last ====================

    @Nested
    @DisplayName("When the added player COMPLETES the lobby")
    class LastPlayer {

        // Note: addPlayer internally calls new BoardBuilder().createBoard(REQUIRED)
        // which reads game_data.json and builds a real Board. It cannot be mocked,
        // so we use a real Game for the phase transition tests.

        private Game realGame;

        @BeforeEach
        void setUpRealGame() {
            realGame = new Game("G-TEST", REQUIRED);
        }

        @Test
        @DisplayName("should add all players and transition to PlaceTotemPhase when lobby is full")
        void shouldTransitionToPlaceTotemPhaseWhenFull() {
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Alice");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Bob");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Charlie");

            assertInstanceOf(PlaceTotemPhase.class, realGame.getCurrentPhase());
        }

        @Test
        @DisplayName("should set a non-null Board on the game when lobby is full")
        void shouldSetBoardWhenFull() {
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Alice");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Bob");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Charlie");

            assertNotNull(realGame.getBoard());
        }

        @Test
        @DisplayName("should add all REQUIRED players to the context")
        void shouldAddAllPlayersToContext() {
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Alice");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Bob");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Charlie");

            assertEquals(REQUIRED, realGame.getPlayerContext().getPlayers().size());
        }

        @Test
        @DisplayName("should assign unique totems to all players")
        void shouldAssignUniqueTotemsToAllPlayers() {
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Alice");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Bob");
            phase.addPlayer(realGame.getPlayerContext(), realGame, "Charlie");

            List<Totem> totems = realGame.getPlayerContext().getPlayers().stream()
                    .map(Player::getTotem)
                    .toList();
            assertEquals(REQUIRED, totems.stream().distinct().count());
        }
    }
}