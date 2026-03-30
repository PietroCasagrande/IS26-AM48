package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;

public class Board {
    private final OfferCardTrack track;
    private final OfferTurnCard turnOrder;
    private final Deck<Card> tribeDeck;
    private final Deck<BuildingCard> buildingDeck;
    private final Showed<Card> tribeShowed;
    private final Showed<BuildingCard> buildingsShowed;

    public Board(OfferCardTrack track, OfferTurnCard turnOrder, Deck<Card> tribeDeck, Deck<BuildingCard> buildingDeck, Showed<Card> tribeShowed, Showed<BuildingCard> buildingsShowed){
        this.track = track;
        this.turnOrder = turnOrder;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.tribeShowed = tribeShowed;
        this.buildingsShowed = buildingsShowed;
    }

    public void placeTotem(Player player, char position){ throw new UnsupportedOperationException("TODO"); }
    public List<Player> getActionOrder(){ throw new UnsupportedOperationException("TODO"); }
    public Card takeCard(Player player, String cardId){ throw new UnsupportedOperationException("TODO"); }
    public boolean isCardTop(String cardId){ throw new UnsupportedOperationException("TODO"); }
    public boolean isCardDown(String cardId){ throw new UnsupportedOperationException("TODO"); }
    public OfferCard findTrackPosition(Player player){ throw new UnsupportedOperationException("TODO"); }
    public void endTurn(int numPlayers){ throw new UnsupportedOperationException("TODO"); }
    public void displayTribeCards(int numPlayers) { throw new UnsupportedOperationException("TODO"); }
    public void displayBuildings(int numPlayers) { throw new UnsupportedOperationException("TODO"); }

    // getter (?)
    public Showed<Card> getTribeShowed() { return tribeShowed; }
    public Showed<BuildingCard> getBuildingShowed() { return buildingsShowed; }

    // BoardSnapshot getSnapshot()

    // metodo setupTurn()
}
