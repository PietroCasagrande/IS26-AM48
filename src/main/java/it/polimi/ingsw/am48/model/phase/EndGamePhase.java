package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.EndGameDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.EndGamePhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EndGamePhase implements GamePhase {
    public GameDelta resolveEndGame(Game game){
        // activating end-game strategies
        game.getNotificatorCenter().getEndGameNotificator().notify(game.getPlayerContext());

        // computing end-game points for each player
        game.getPlayerContext().getPlayers()
                .forEach(p -> p.getTribe().computeTotalEndGameScore());

        // building the final scores map
        Map<String, Integer> finalScores = new HashMap<>();
        game.getPlayerContext().getPlayers()
                .forEach(p -> finalScores.put(p.getNickname(), p.getPoints()));

        // finding game's winner
        Player winner = game.findWinner();

        // returns the Delta containing the final score of each player, and winner's nickname
        return new EndGameDelta(finalScores, winner.getNickname());
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndGamePhaseSnapshot();
    }
}
