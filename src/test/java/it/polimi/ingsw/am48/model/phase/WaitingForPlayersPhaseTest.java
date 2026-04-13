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
    @Mock private Board board;


    private static final int REQUIRED = 3; // simula partita da 3 giocatori
    private WaitingForPlayersPhase phase;

    @BeforeEach
    void setUp() {
        phase = new WaitingForPlayersPhase(REQUIRED);
    }

    // toSnapshot
    @Test
    @DisplayName("toSnapshot restituisce un WaitingPhaseSnapshot")
    void toSnapshot_returnsCorrectType() {
        assertInstanceOf(WaitingPhaseSnapshot.class, phase.toSnapshot());
    }

    // addPlayer – giocatore NON è l'ultimo
    @Nested
    @DisplayName("Quando il giocatore aggiunto NON completa la lobby")
    class NotLastPlayer {

        @Test
        @DisplayName("Viene creato un Player con il totem corretto (indice 0 → BLACK)")
        void addPlayer_firstPlayer_assignsFirstTotem() {
            when(playerContext.getPlayers()).thenReturn(new ArrayList<>());

            phase.addPlayer(playerContext, game, "Alice");

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
            verify(playerContext).addPlayer(captor.capture());
            Player added = captor.getValue();

            assertEquals("Alice", added.getNickname());
            assertEquals(Totem.values()[0], added.getTotem());
        }

        @Test
        @DisplayName("Viene creato un Player con il totem corretto (indice 1 → BLUE)")
        void addPlayer_secondPlayer_assignsSecondTotem() {
            List<Player> existingPlayers = new ArrayList<>();
            existingPlayers.add(mock(Player.class));
            when(playerContext.getPlayers()).thenReturn(existingPlayers);

            phase.addPlayer(playerContext, game, "Bob");

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
            verify(playerContext).addPlayer(captor.capture());
            assertEquals(Totem.values()[1], captor.getValue().getTotem());
        }

        @Test
        @DisplayName("setupBoard NON viene chiamato")
        void addPlayer_notLastPlayer_doesNotSetupBoard() {
            when(playerContext.getPlayers()).thenReturn(new ArrayList<>());

            phase.addPlayer(playerContext, game, "Alice");

            verify(game, never()).getBoard();
        }

        @Test
        @DisplayName("setPhase NON viene chiamato")
        void addPlayer_notLastPlayer_doesNotChangePhase() {
            when(playerContext.getPlayers()).thenReturn(new ArrayList<>());

            phase.addPlayer(playerContext, game, "Alice");

            verify(game, never()).setPhase(any());
        }
    }

    // 3. addPlayer – giocatore È l'ultimo (index == requiredPlayer)
    @Nested
    @DisplayName("Quando il giocatore aggiunto COMPLETA la lobby")
    class LastPlayer {

        private List<Player> fullLobby;

        @BeforeEach
        void setUpFullLobby() {
            fullLobby = new ArrayList<>();
            for (int i = 0; i < REQUIRED; i++) {
                fullLobby.add(mock(Player.class));
            }
            when(playerContext.getPlayers()).thenReturn(fullLobby);
            when(game.getBoard()).thenReturn(board);
            when(board.getPlaceOrder()).thenReturn(fullLobby);
        }

        @Test
        @DisplayName("setupBoard viene chiamato con i giocatori del contesto")
        void addPlayer_lastPlayer_callsSetupBoard() {
            phase.addPlayer(playerContext, game, "Charlie");

            verify(board).setupBoard(fullLobby);
        }

        @Test
        @DisplayName("setPhase viene chiamato con una PlaceTotemPhase")
        void addPlayer_lastPlayer_transitionsToPlaceTotemPhase() {
            phase.addPlayer(playerContext, game, "Charlie");

            ArgumentCaptor<GamePhase> captor = ArgumentCaptor.forClass(GamePhase.class);
            verify(game).setPhase(captor.capture());
            assertInstanceOf(PlaceTotemPhase.class, captor.getValue());
        }

        @Test
        @DisplayName("Il player viene comunque aggiunto al contesto prima del setup")
        void addPlayer_lastPlayer_playerIsStillAdded() {
            phase.addPlayer(playerContext, game, "Charlie");

            verify(playerContext).addPlayer(any(Player.class));
        }

        @Test
        @DisplayName("getPlaceOrder viene usato per costruire PlaceTotemPhase")
        void addPlayer_lastPlayer_usesGetPlaceOrder() {
            phase.addPlayer(playerContext, game, "Charlie");

            verify(board).getPlaceOrder();
        }
    }
}