package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.delta.EndTurnDelta;
import it.polimi.ingsw.am48.model.delta.EventInfo;
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
        List<EventInfo> eventInfos = new ArrayList<>();

        // resolve events, capturing deltas for each
        for(EventType e : EventType.values()){
            Map<String, Integer> foodBefore = snapshotFood(game);
            Map<String, Integer> pointsBefore = snapshotPoints(game);

            game.getNotificatorCenter().getEventNotificator().notify(e, game.getPlayerContext());

            Map<String, Integer> foodAfter = snapshotFood(game);
            Map<String, Integer> pointsAfter = snapshotPoints(game);

            Map<String, Integer> foodDeltas = new HashMap<>();
            Map<String, Integer> pointsDeltas = new HashMap<>();
            for (String nick : foodAfter.keySet()) {
                foodDeltas.put(nick, foodAfter.get(nick) - foodBefore.get(nick));
                pointsDeltas.put(nick, pointsAfter.get(nick) - pointsBefore.get(nick));
            }

            eventInfos.add(new EventInfo(e.name(), foodDeltas, pointsDeltas));
        }

        // board update after solving events
        if(game.getCurrentTurn() < 10) game.getBoard().endTurn(game.getNotificatorCenter(), game.getPlayerContext());

        // turn increment
        game.incrementTurn();

        // end game check
        if(game.getCurrentTurn() > 10){
            EndGamePhase endGamePhase = new EndGamePhase();
            game.setPhase(endGamePhase);
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "END_GAME", eventInfos));
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "END_GAME", game.getBoard().getCurrEraIndex()));
            deltas.add(endGamePhase.resolveEndGame(game));
        } else {
            game.setPhase(new PlaceTotemPhase());
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "PLACE_TOTEM", eventInfos));
            deltas.add(buildEndTurnDelta(game, game.getCurrentTurn(), "PLACE_TOTEM",  game.getBoard().getCurrEraIndex()));
        }

        return deltas;
    }

    private Map<String, Integer> snapshotFood(Game game) {
        Map<String, Integer> map = new HashMap<>();
        game.getPlayerContext().getPlayers().forEach(p ->
                map.put(p.getNickname(), p.getFood()));
        return map;
    }

    private Map<String, Integer> snapshotPoints(Game game) {
        Map<String, Integer> map = new HashMap<>();
        game.getPlayerContext().getPlayers().forEach(p ->
                map.put(p.getNickname(), p.getPoints()));
        return map;
    }

    private EndTurnDelta buildEndTurnDelta(Game game, int newTurn, String newPhase, int newEra) {
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
                newPhase,
                eventInfos
                newPhase,
                newEra
        );
    }

    @Override
    public PhaseSnapshot toSnapshot(){
        return new EndTurnPhaseSnapshot();
    }
}
