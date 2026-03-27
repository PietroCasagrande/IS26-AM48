package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;

public class Showed<T>{
    private List<T> upperList;
    private List<T> lowerList;

    public void addCard(List<T> cl){
        throw new UnsupportedOperationException("TODO");
    }

    public void shiftRow(){
        throw new UnsupportedOperationException("TODO");
    }

    public void clearBottom(){
        throw new UnsupportedOperationException("TODO");
    }

    public boolean diffLastEras(){
        // controlla era di ultimo elemento di upper e ultimo di lowe
        // se sono diverse return true (era cambiata)
        throw new UnsupportedOperationException("TODO");
    }

    public T takeCard(Player p, String id){
        throw new UnsupportedOperationException("TODO");
    }

    // public getSnapshot(){}; bisogna capire cosa returna nello specifico

    public boolean isTop(String id){
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isDown(String id){
        throw new UnsupportedOperationException("TODO");
    }

}
