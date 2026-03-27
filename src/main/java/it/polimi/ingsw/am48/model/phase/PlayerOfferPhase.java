package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;

public class PlayerOfferPhase implements GamePhase, PickContext {
    private List<Player> actionOrder;
    private int currIdx;
    private int pickFromUp;
    private int PickFromDown;

    @Override
    public GameDelta takeCard(Game game, Player player, String cardId){
        throw new UnsupportedOperationException("TODO");
    }

    private void onPickNotify(Game game, Player player){
        throw new UnsupportedOperationException("TODO");
    }

    private void onEndOfferPhaseNotify(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    private void onTotemReturnedNotify(Game game){
        throw new UnsupportedOperationException("TODO");
    }

    // @Override ?
    public void getPicksUp(){
        throw new UnsupportedOperationException("TODO");
    }

    // @Override ?
    public void getPicksDown(){
        throw new UnsupportedOperationException("TODO");
    }

    // @Override ?
    public void nextPlayer(){
        throw new UnsupportedOperationException("TODO");
    }

    // @Override ?
    public void increasePicksUp(){
        throw new UnsupportedOperationException("TODO");
    }

    // @Override ?
    public void increasePicksDown(){
        throw new UnsupportedOperationException("TODO");
    }

}
