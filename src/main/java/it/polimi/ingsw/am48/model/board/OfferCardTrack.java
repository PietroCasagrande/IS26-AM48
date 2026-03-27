package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;
import java.util.Map;

public class OfferCardTrack {
    private final Map<Character, OfferCard> track;

    public OfferCardTrack(Map<Character, OfferCard> track){
        this.track = track;
    }

    public void placeTotem(Player player, char position){ throw new UnsupportedOperationException("TODO"); }
    public List<Player> getActionOrder(){ throw new UnsupportedOperationException("TODO"); }
    public OfferCard findTrackPosition(Player player){ throw new UnsupportedOperationException("TODO"); }

    // getSnapshot()
}
