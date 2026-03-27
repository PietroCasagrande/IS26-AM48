package it.polimi.ingsw.am48.model.board;

import java.util.List;

public class Deck<T> {
    private List<T> deck; // forse non serve generic anche per farne due: BuildingCard è comunque una Card quindi si potrebbe fare List<Card>

    public List<T> fishCard(int n){
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEmpty(){
        throw new UnsupportedOperationException("TODO");
    }

    public List<T> getSnapshot() {
        throw new UnsupportedOperationException("TODO");
    }
}
