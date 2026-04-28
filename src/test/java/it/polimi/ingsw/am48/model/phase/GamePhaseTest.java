package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GamePhaseTest {

    private static class TestPhase implements GamePhase {
        @Override
        public PhaseSnapshot toSnapshot() {
            return null;
        }
    }

    private final GamePhase phase = new TestPhase();

    @Test
    @DisplayName("addPlayer: should throw InvalidActionException by default")
    void addPlayerShouldThrowByDefault() {
        PlayerContext mockCtx = mock(PlayerContext.class);
        Game mockGame = mock(Game.class);

        assertThrows(InvalidActionException.class, () ->
                phase.addPlayer(mockCtx, mockGame, "Player1")
        );
    }

    @Test
    @DisplayName("placeTotem: should throw InvalidActionException by default")
    void placeTotemShouldThrowByDefault() {
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);

        assertThrows(InvalidActionException.class, () ->
                phase.placeTotem(mockGame, mockPlayer, 'A')
        );
    }

    @Test
    @DisplayName("takeCard: should throw InvalidActionException by default")
    void takeCardShouldThrowByDefault() {
        Game mockGame = mock(Game.class);
        Player mockPlayer = mock(Player.class);

        assertThrows(InvalidActionException.class, () ->
                phase.takeCard(mockGame, mockPlayer, "card_id")
        );
    }
}