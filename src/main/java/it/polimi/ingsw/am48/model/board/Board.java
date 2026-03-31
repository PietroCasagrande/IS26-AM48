package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;
import java.util.Optional;

public class Board {
    private final OfferCardTrack track;
    private final OfferTurnCard turnOrder;
    private final Deck<Card> tribeDeck;
    private final Deck<BuildingCard> buildingDeck;
    private final Showed<Card> tribeShowed;
    private final Showed<BuildingCard> buildingShowed;
    private final List <Integer> buildingsPerEra;
    private Era currEra;
    private final int numPlayers;

    public Board(OfferCardTrack track, OfferTurnCard turnOrder, Deck<Card> tribeDeck, Deck<BuildingCard> buildingDeck, Showed<Card> tribeShowed, Showed<BuildingCard> buildingsShowed, List<Integer> buildingsPerEra, int numPlayers) {
        this.track = track;
        this.turnOrder = turnOrder;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.tribeShowed = tribeShowed;
        this.buildingShowed = buildingsShowed;
        this.buildingsPerEra = List.copyOf(buildingsPerEra);
        this.currEra = Era.FIRST;
        this.numPlayers = numPlayers;
    }

    // Places player totem on the offer track
    public void placeTotem(Player player, char position){
        this.track.placeTotem(player, position);
    }

    // Returns offer phase order based on players' placements
    public List<Player> getActionOrder(){
        return this.track.getActionOrder();
    }

    // Takes the requested card from the showed
    // throwing exception is necessary to return a Card instead of a Optional<Card>
    public Card takeCard(Player player, String cardId){
        return this.tribeShowed.takeCard(player, cardId)
                .or(() -> this.buildingShowed.takeCard(player, cardId))
                .orElseThrow(() -> new IllegalArgumentException("Card " + cardId + " not found"));
    }

    // Returns true if the requested card is displayed on the upper row, false otherwise
    public boolean isCardTop(String cardId){
        return this.tribeShowed.isTop(cardId) || this.buildingShowed.isTop(cardId);
    }

    // Returns true  if the requested card is displayed on the lower row, false otherwise
    public boolean isCardDown(String cardId){
        return this.tribeShowed.isDown(cardId) || this.buildingShowed.isDown(cardId);
    }

    // Finds the offer track on which the requested player totem lies
    public OfferCard findTrackPosition(Player player){
        return this.track.findTrackPosition(player);
    }

    // Initialize the board for the very first turn
    public void setupBoard(List<Player> players){
        this.tribeShowed.addLowerCards(this.tribeDeck.drawCards(this.numPlayers + 1));
        this.displayTribeCards();
        this.displayBuildings();
        this.turnOrder.setupOrder(players);
    }

    public void endTurn(){
        this.tribeShowed.clearBottom();
        this.tribeShowed.shiftRow();
        this.displayTribeCards();
    }

    public void changeEra(){
        this.currEra = this.currEra.next();
        this.buildingShowed.clearBottom();
        this.buildingShowed.shiftRow();
        this.displayBuildings();
    }

    // Draws tribe cards from the deck and displays them on the tribe showed
    private void displayTribeCards() {
        this.tribeShowed.addUpperCards(this.tribeDeck.drawCards(this.numPlayers + 4));
    }

    // Draws building cards from the deck and displays them on the tribe showed
    private void displayBuildings() {
        this.buildingShowed.addUpperCards(this.buildingDeck.drawCards(this.buildingsPerEra.get(this.currEra.getIndex())));
    }

    // getter (?)
    public Showed<Card> getTribeShowed() { return tribeShowed; }
    public Showed<BuildingCard> getBuildingShowed() { return buildingShowed; }

    // BoardSnapshot getSnapshot()

    // metodo setupTurn()
}
