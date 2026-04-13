package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.Showed;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.delta.EndGameDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.enums.*;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.notificator.OnEndGameNotificator;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EndGamePhaseTest {

    private Game game;
    private PlayerContext playerContext;
    private NotificatorCenter nc;
    private OnEndGameNotificator endGameNotificator;
    private EndGamePhase phase;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("Sveva", Totem.RED);
        Player p2 = new Player("Ilaria", Totem.BLUE);
        Player p3 = new Player("Charlotte", Totem.WHITE);

        playerContext = new PlayerContext();
        playerContext.addPlayer(p1);
        playerContext.addPlayer(p2);
        playerContext.addPlayer(p3);

        endGameNotificator = mock(OnEndGameNotificator.class);
        nc = mock(NotificatorCenter.class);
        when(nc.getEndGameNotificator()).thenReturn(endGameNotificator);

        game = mock(Game.class);
        when(game.getPlayerContext()).thenReturn(playerContext);
        when(game.getNotificatorCenter()).thenReturn(nc);

        phase = new EndGamePhase();
    }

    // resolveEndGame should call endGame notificator
    @Test
    void resolveEndGameShouldNotifyEndGameListeners() {
        // findWinner needs to work on real players
        when(game.findWinner()).thenReturn(playerContext.getPlayers().getFirst());

        phase.resolveEndGame(game);

        verify(nc.getEndGameNotificator()).notify(playerContext);
    }

    // resolveEndGame should compute scores for all players
    @Test
    void resolveEndGameShouldComputeScoresForAllPlayers() {
        Player p1 = playerContext.getPlayers().get(0);
        Player p2 = playerContext.getPlayers().get(1);
        Player p3 = playerContext.getPlayers().get(2);

        // Give players some points and characters for scoring
        p1.updatePoints(19);
        p1.addToTribe(new CharacterCard("A1", Era.FIRST, null, CharacterType.ARTIST, 2));
        p1.addToTribe(new CharacterCard("A2", Era.FIRST, null, CharacterType.ARTIST, 2));
        // 2 artists = 1 pair = 10 PP

        p2.updatePoints(30);
        p3.updatePoints(5);

        when(game.findWinner()).thenReturn(p2);

        EndGameDelta delta = (EndGameDelta) phase.resolveEndGame(game);

        // p1: 19 base + 10 artists = 29
        assertEquals(29, delta.getFinalScores().get("Sveva"));
        assertEquals(30, delta.getFinalScores().get("Ilaria"));
        assertEquals(5, delta.getFinalScores().get("Charlotte"));
    }

    // resolveEndGame should return correct winner
    @Test
    void resolveEndGameShouldReturnCorrectWinner() {
        playerContext.getPlayers().get(0).updatePoints(10);
        playerContext.getPlayers().get(1).updatePoints(50);
        playerContext.getPlayers().get(2).updatePoints(25);

        when(game.findWinner()).thenReturn(playerContext.getPlayers().get(1));

        EndGameDelta delta = (EndGameDelta) phase.resolveEndGame(game);

        assertEquals("Ilaria", delta.getWinnerNickname());
    }

    // resolveEndGame should include all players in finalScores
    @Test
    void resolveEndGameShouldIncludeAllPlayersInScores() {
        when(game.findWinner()).thenReturn(playerContext.getPlayers().getFirst());

        EndGameDelta delta = (EndGameDelta) phase.resolveEndGame(game);

        assertEquals(3, delta.getFinalScores().size());
        assertTrue(delta.getFinalScores().containsKey("Sveva"));
        assertTrue(delta.getFinalScores().containsKey("Ilaria"));
        assertTrue(delta.getFinalScores().containsKey("Charlotte"));
    }
}