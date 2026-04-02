package it.polimi.ingsw.am48.model.factory;

import java.util.List;

public interface BoardFactory<T> {
    public List<T> createCards(int numPlayers);
}
