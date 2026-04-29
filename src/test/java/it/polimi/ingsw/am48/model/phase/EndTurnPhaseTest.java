package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.EndGameDelta;
import it.polimi.ingsw.am48.model.delta.EndTurnDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.notificator.OnEndGameNotificator;
import it.polimi.ingsw.am48.model.notificator.OnEventNotificator;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EndTurnPhaseTest {

    private Game game;
    private Board board;
    private PlayerContext playerContext;
    private NotificatorCenter nc;
    private OnEventNotificator eventNotificator;
    private OnEndGameNotificator endGameNotificator;
    private Showed<Card> tribeShowed;
    private Showed<BuildingCard> buildingShowed;
    private EndTurnPhase phase;

    @BeforeEach
    void setUp() {
        // Real players
        Player p1 = new Player("Sveva", Totem.RED);
        Player p2 = new Player("Ilaria", Totem.BLUE);
        p1.updateFood(5);
        p2.updateFood(3);

        playerContext = new PlayerContext();
        playerContext.addPlayer(p1);
        playerContext.addPlayer(p2);

        // Mock notificators
        eventNotificator = mock(OnEventNotificator.class);
        endGameNotificator = mock(OnEndGameNotificator.class);
        nc = mock(NotificatorCenter.class);
        when(nc.getEventNotificator()).thenReturn(eventNotificator);
        when(nc.getEndGameNotificator()).thenReturn(endGameNotificator);

        // Mock showed with empty lists
        tribeShowed = mock(Showed.class);
        when(tribeShowed.getUpperList()).thenReturn(List.of());
        when(tribeShowed.getLowerList()).thenReturn(List.of());

        buildingShowed = mock(Showed.class);
        when(buildingShowed.getUpperList()).thenReturn(List.of());
        when(buildingShowed.getLowerList()).thenReturn(List.of());

        // Mock board
        board = mock(Board.class);
        when(board.getTribeShowed()).thenReturn(tribeShowed);
        when(board.getBuildingShowed()).thenReturn(buildingShowed);
        when(board.getPlaceOrder()).thenReturn(List.of(p1, p2));

        // Mock game
        game = mock(Game.class);
        when(game.getPlayerContext()).thenReturn(playerContext);
        when(game.getNotificatorCenter()).thenReturn(nc);
        when(game.getBoard()).thenReturn(board);
        when(game.getCurrentTurn()).thenReturn(1);

        phase = new EndTurnPhase();
    }

    // endTurn should resolve all event types
    @Test
    void endTurnShouldResolveAllEvents() {
        phase.endTurn(game);

        for (EventType e : EventType.values()) {
            verify(eventNotificator).notify(eq(e), eq(playerContext));
        }
    }

    // endTurn should call board.endTurn
    @Test
    void endTurnShouldUpdateBoard() {
        phase.endTurn(game);

        verify(board).endTurn(nc, playerContext);
    }

    // endTurn should increment turn
    @Test
    void endTurnShouldIncrementTurn() {
        phase.endTurn(game);

        verify(game).incrementTurn();
    }

    // endTurn should return EndTurnDelta with updated player resources
    @Test
    void endTurnShouldReturnDeltaWithUpdatedResources() {
        List<GameDelta> deltas = phase.endTurn(game);

        assertTrue(deltas.get(0) instanceof EndTurnDelta);
        EndTurnDelta delta = (EndTurnDelta) deltas.get(0);
        assertEquals(5, delta.getUpdatedFood().get("Sveva"));
        assertEquals(3, delta.getUpdatedFood().get("Ilaria"));
    }

    // endTurn on turn 10 should transition to EndGamePhase
    @Test
    void endTurnOnLastRoundShouldTransitionToEndGame() {
        // After incrementTurn, currentTurn will be checked
        // We need getCurrentTurn to return > 10 after increment
        when(game.getCurrentTurn()).thenReturn(11);
        when(game.findWinner()).thenReturn(playerContext.getPlayers().get(0));

        List<GameDelta> deltas = phase.endTurn(game);

        // Should have EndTurnDelta + EndGameDelta
        assertEquals(2, deltas.size());
        assertTrue(deltas.get(0) instanceof EndTurnDelta);
        assertTrue(deltas.get(1) instanceof EndGameDelta);
        verify(game).setPhase(any(EndGamePhase.class));
    }

    // endTurn before turn 10 should transition to PlaceTotemPhase
    @Test
    void endTurnBeforeLastRoundShouldTransitionToPlaceTotem() {
        when(game.getCurrentTurn()).thenReturn(3);

        List<GameDelta> deltas = phase.endTurn(game);

        assertEquals(1, deltas.size());
        assertTrue(deltas.get(0) instanceof EndTurnDelta);
        verify(game).setPhase(any(PlaceTotemPhase.class));
    }

    // endTurn delta should contain board state
    @Test
    void endTurnDeltaShouldContainBoardState() {
        List<GameDelta> deltas = phase.endTurn(game);
        EndTurnDelta delta = (EndTurnDelta) deltas.get(0);

        assertNotNull(delta.getNewUpperTribeIds());
        assertNotNull(delta.getNewLowerTribeIds());
        assertNotNull(delta.getNewUpperBuildingIds());
        assertNotNull(delta.getNewLowerBuildingIds());
    }
}