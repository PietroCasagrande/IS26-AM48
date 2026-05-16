package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.BoardSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public Board(OfferCardTrack track, OfferTurnCard turnOrder, Deck<Card> tribeDeck,
                 Deck<BuildingCard> buildingDeck, List<Integer> buildingsPerEra, int numPlayers) {
        this.track = track;
        this.turnOrder = turnOrder;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.tribeShowed = new Showed<>();
        this.buildingShowed = new Showed<>();
        this.buildingsPerEra = List.copyOf(buildingsPerEra);
        this.currEra = Era.FIRST;
        this.numPlayers = numPlayers;
    }

    // Places player totem on the offer track
    public void placeTotem(Player player, char position){
        Player expectedPlayer = this.turnOrder.getNextTotem();
        if(expectedPlayer == null || !expectedPlayer.equals(player)){
            throw new IllegalStateException("Cannot place totem: wait for your turn.");
        }
        this.track.placeTotem(expectedPlayer, position);
        this.turnOrder.removeNextTotem();
    }

    // Places player totem on offer turn card after player picks
    public void returnTotem(Player player) {this.turnOrder.returnTotem(player);}

    // Returns offer phase order based on players' placements
    public List<Player> getPickOrder(){
        return this.track.getPickOrder();
    }

    // Takes the requested card from the showed
    // throwing exception is necessary to return a Card instead of an Optional<Card>
    public Card takeCard(PlayerContext playerContext, String cardId){
        return this.tribeShowed.takeCard(playerContext, cardId)
                .or(() -> this.buildingShowed.takeCard(playerContext, cardId))
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

    // Ends the current turn and sets up the next one
    public void endTurn(NotificatorCenter nc, PlayerContext playerContext){
        this.tribeShowed.clearBottom();
        this.tribeShowed.shiftRow();
        this.tribeShowed.registerBottom(nc, playerContext);
        this.displayTribeCards();
        if(this.tribeShowed.diffLastEras()) this.changeEra();
    }

    // Changes displayed building cards due to Era changing
    private void changeEra(){
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

    // takes the order attribute from OfferTurnCard
    public List <Player> getPlaceOrder(){return turnOrder.getPlaceOrder(); }
    public BoardSnapshot toSnapshot(){
        List<String> upperRowIds = tribeShowed.getUpperList().stream()
                .map(Card::getCardId)
                .toList();

        List<String> lowerRowIds = tribeShowed.getLowerList().stream()
                .map(Card::getCardId)
                .toList();

        List<String> buildingUpperIds = buildingShowed.getUpperList().stream()
                .map(Card::getCardId)
                .toList();

        List<String> buildingLowerIds = buildingShowed.getLowerList().stream()
                .map(Card::getCardId)
                .toList();

        List<String> tribeDeckIds = tribeDeck.getRemainingCardIds();
        List<String> buildingDeckIds = buildingDeck.getRemainingCardIds(); // TODO: se vogliamo il deck di building separato per ere, dovremo salvarlo come Map<String, List<String>> per lo Snapshot

        return new BoardSnapshot(
                upperRowIds,
                lowerRowIds,
                buildingUpperIds,
                buildingLowerIds,
                tribeDeckIds,
                buildingDeckIds,
                track.toSnapshot(),
                turnOrder.toSnapshot(),
                buildingsPerEra,
                currEra.name(),
                numPlayers
        );
    }

    public static Board fromSnapshot(BoardSnapshot snapshot, Map<String, Card> cardMap, List<Player> players){
        List<Card> tribeDeck = new ArrayList<>();
        for(String id : snapshot.getTribeDeckRemainingIds()){
            Card card = cardMap.get(id);
            if (card == null) {
                throw new IllegalStateException("Errore critico: Il character con ID " + id + " non è presente nel salvataggio sul disco!");
            }
            tribeDeck.add(card);
        }
        List<BuildingCard> buildingDeck = new ArrayList<>();
        for(String id : snapshot.getBuildingDeckRemainingIds()){
            Card card = cardMap.get(id);
            if (card == null) {
                throw new IllegalStateException("Errore critico: Il building con ID " + id + " non è presente nel salvataggio sul disco!");
            }
            buildingDeck.add((BuildingCard) card);
        }

        Board board = new Board(
                OfferCardTrack.fromSnapshot(snapshot.getOfferTrack(), snapshot.getNumPlayers(), players),
                OfferTurnCard.fromSnapshot(snapshot.getOfferTurnCard(), players),
                new Deck<>(tribeDeck),
                new Deck<>(buildingDeck),
                snapshot.getBuildingsPerEra(),
                snapshot.getNumPlayers()
        );

        List<Card> upperCards = new ArrayList<>();
        for(String id : snapshot.getUpperRowCardIds()) {
            upperCards.add(cardMap.get(id));
        }
        board.tribeShowed.addUpperCards(upperCards);

        List<Card> lowerCards = new ArrayList<>();
        for(String id : snapshot.getLowerRowCardIds()) {
            lowerCards.add(cardMap.get(id));
        }
        board.tribeShowed.addLowerCards(lowerCards);

        List<BuildingCard> upperBuildings = new ArrayList<>();
        for(String id : snapshot.getBuildingUpperRowCardIds()){
            upperBuildings.add((BuildingCard) cardMap.get(id));
        }
        board.buildingShowed.addUpperCards(upperBuildings);

        List<BuildingCard> lowerBuildings = new ArrayList<>();
        for(String id : snapshot.getBuildingLowerRowCardIds()){
            lowerBuildings.add((BuildingCard) cardMap.get(id));
        }
        board.buildingShowed.addLowerCards(lowerBuildings);

        board.currEra = Era.valueOf(snapshot.getCurrEra());

        return board;
    }

}
