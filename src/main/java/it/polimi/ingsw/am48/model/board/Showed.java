package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Showed<T extends Card>{
    private final List<T> upperList;
    private final List<T> lowerList;

    // initialization of the two lists
    public Showed(){
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
    }

    // getter needed by board
    public List<T> getUpperList() { return List.copyOf(upperList); }
    public List<T> getLowerList() { return List.copyOf(lowerList); }

    // adds cardsList to upperList on each round, the deck chooses the size() and the cards for cardsList
    public void addUpperCards(List<T> cardsList){
        upperList.addAll(cardsList);
    }

    // adds cardsList to lowerList at the beginning of the game, later on we'll use the shiftRow method
    public void addLowerCards(List<T> cardsList){
        lowerList.addAll(cardsList);
    }

    // moves the cards from the upperList to the lowerList
    // tribeShowed upperList will be shifted at the end of each Turn
    // buildingShowed upperList instead will be shifted only at the era exchange
    public void shiftRow(){
        lowerList.addAll(upperList);
        upperList.clear();
    }

    // Registers events contained in the bottom row
    public void registerBottom(NotificatorCenter nc, PlayerContext playerContext){
        // Sort is necessary for corner case of two events of the same type in the same round
        List<T> sortedCards = this.lowerList.stream()
                .sorted((c1, c2) -> Integer.compare(c1.getEra().getIndex(), c2.getEra().getIndex()))
                .toList();
        for(T card : sortedCards)
            if (card.getStrategy() != null)
                card.getStrategy().registerTo(nc, playerContext);
    }

    // Registers events contained in the upper row (for the last turn)
    public void registerUpper(NotificatorCenter nc, PlayerContext playerContext){
        List<T> sortedCards = this.upperList.stream()
                .sorted((c1, c2) -> Integer.compare(c1.getEra().getIndex(), c2.getEra().getIndex()))
                .toList();
        for(T card : sortedCards)
            if (card.getStrategy() != null)
                card.getStrategy().registerTo(nc, playerContext);
    }

    public void clearBottom(){
        lowerList.clear();
    }

    // checks if the era of upperList's last card and the era of lowerList's last card
    // if the two are different, returns true (different eras)
    public boolean diffLastEras(){
        if(upperList.isEmpty() || lowerList.isEmpty()) return false;
        return upperList.getLast().getEra() != lowerList.getLast().getEra();
    }

    // takeCard is used inside of Board's method "takeCard(player, cardId)"
    public Optional<T> takeCard(PlayerContext playerContext, String cardId){
        // search the cardId in the upperList first
        Iterator<T> upperIt = upperList.iterator();
        while(upperIt.hasNext()){
            T card = upperIt.next();
            if(card.getCardId().equals(cardId)){
                card.acquire(playerContext);
                upperIt.remove();
                return Optional.of(card);
            }
        }

        // search the cardId in the lowerList if it isn't in the upperList
        Iterator<T> lowerIt = lowerList.iterator();
        while(lowerIt.hasNext()){
            T card = lowerIt.next();
            if(card.getCardId().equals(cardId)){
                card.acquire(playerContext);
                lowerIt.remove();
                return Optional.of(card);
            }
        }
        return Optional.empty();
    }

    // checks if the card belongs to the upperList: used in Board's method "isCardTop()"
    public boolean isTop(String cardId){
        return upperList.stream().anyMatch(c -> c.getCardId().equals(cardId));
    }

    // checks if the card belongs to the lowerList: used in Board's method "isCardDown()"
    public boolean isDown(String cardId){
        return lowerList.stream().anyMatch(c -> c.getCardId().equals(cardId));
    }

    // public getSnapshot(){}; bisogna capire cosa restituisce nello specifico
}
