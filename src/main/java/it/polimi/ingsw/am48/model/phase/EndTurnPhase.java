package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.EndTurnDelta;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.snapshot.EndTurnPhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndTurnPhase implements GamePhase {

    public List<GameDelta> endTurn(Game game){
        List<GameDelta> deltas = new ArrayList<>();

        // resolve events
        for(EventType e : EventType.values()){
            game.getNotificatorCenter().getEventNotificator().notify(e, game.getPlayerContext());
        }

        // board update after solving events
        if(game.getCurrentTurn() < 10) game.getBoard().endTurn(game.getNotificatorCenter(), game.getPlayerContext());

        // turn increment
        game.incrementTurn();

        // end game check
        if(game.getCurrentTurn() > 10){
            EndGamePhase endGamePhase = new EndGamePhase();
            game.setPhase(endGamePhase);
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "END_GAME"));
            deltas.add(endGamePhase.resolveEndGame(game));
        } else {
            game.setPhase(new PlaceTotemPhase());
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "PLACE_TOTEM"));
        }

        return deltas;
    }


    private EndTurnDelta buildEndTurnDelta(Game game, int newTurn, String newPhase) {
        // Updated resources for each player (after events)
        Map<String, Integer> updatedFood = new HashMap<>();
        Map<String, Integer> updatedPrestige = new HashMap<>();
        game.getPlayerContext().getPlayers().forEach(p -> {
            updatedFood.put(p.getNickname(), p.getFood());
            updatedPrestige.put(p.getNickname(), p.getPoints());
        });

        // New cards on board after shiftRow
        List<String> newUpperRowIds = game.getBoard().getTribeShowed()
                .getUpperList().stream().map(Card::getCardId).toList();
        List<String> newLowerRowIds = game.getBoard().getTribeShowed()
                .getLowerList().stream().map(Card::getCardId).toList();
        List<String> newBuildingUpperIds = game.getBoard().getBuildingShowed()
                .getUpperList().stream().map(Card::getCardId).toList();
        List<String> newBuildingLowerIds = game.getBoard().getBuildingShowed()
                .getLowerList().stream().map(Card::getCardId).toList();

        return new EndTurnDelta(
                updatedFood,
                updatedPrestige,
                newUpperRowIds,
                newLowerRowIds,
                newBuildingUpperIds,
                newBuildingLowerIds,
                newTurn,
                newPhase
        );
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndTurnPhaseSnapshot();
    }
}
