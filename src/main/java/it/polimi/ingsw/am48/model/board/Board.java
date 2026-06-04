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

/**
 * Acts as the central facade for the game board state.
 * <p>
 * This class aggregates and manages all the physical components shared among players,
 * including the decks, the currently showed cards (tribe and buildings), the offer
 * track for totem placement, and the progression of game eras.
 */
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

    /**
     * Creates a new {@code Board} with the specified components and configuration.
     *
     * @param track           the track managing offer cards and totem placements
     * @param turnOrder       the turn order card tracking the turn progression
     * @param tribeDeck       the deck containing Character and Event cards
     * @param buildingDeck    the deck containing Building cards
     * @param buildingsPerEra a list defining how many building cards to display per era
     * @param numPlayers      the number of players participating in the game
     */
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

    /**
     * Places a player's totem on the specified position of the offer track.
     * Ensures that players act in the correct sequential order.
     *
     * @param player   the player attempting to place the totem
     * @param position the character identifier of the chosen offer track card
     * @throws IllegalStateException if it is not the specified player's turn
     */
    public void placeTotem(Player player, char position){
        Player expectedPlayer = this.turnOrder.getNextTotem();
        if(expectedPlayer == null || !expectedPlayer.equals(player)){
            throw new IllegalStateException("Cannot place totem: wait for your turn.");
        }
        this.track.placeTotem(expectedPlayer, position);
        this.turnOrder.removeNextTotem();
    }

    /**
     * Returns the player's totem to the offer turn card after they complete their pick phase.
     *
     * @param player the player returning their totem
     */
    public void returnTotem(Player player) {this.turnOrder.returnTotem(player);}

    /**
     * Determines the sequential order of players for the offer phase, based on their totem placements.
     *
     * @return an ordered list of players for the pick phase
     */
    public List<Player> getPickOrder(){
        return this.track.getPickOrder();
    }

    /**
     * Attempts to acquire a specific card from the currently displayed rows.
     * The method searches through both the tribe cards and the building cards.
     *
     * @param playerContext the context of the players in game, with current player
     * @param cardId        the unique identifier of the desired card
     * @return the requested {@link Card} instance
     * @throws IllegalArgumentException if the card is not found on the board
     */
    public Card takeCard(PlayerContext playerContext, String cardId){
        return this.tribeShowed.takeCard(playerContext, cardId)
                .or(() -> this.buildingShowed.takeCard(playerContext, cardId))
                .orElseThrow(() -> new IllegalArgumentException("Card " + cardId + " not found"));
    }

    /**
     * Checks if the specified card is currently displayed on the upper row of either the tribe or building displays.
     *
     * @param cardId the unique identifier of the card
     * @return {@code true} if the card is on the upper row, {@code false} otherwise
     */
    public boolean isCardTop(String cardId){
        return this.tribeShowed.isTop(cardId) || this.buildingShowed.isTop(cardId);
    }

    /**
     * Checks if the specified card is currently displayed on the lower row of either the tribe or building displays.
     *
     * @param cardId the unique identifier of the card
     * @return {@code true} if the card is on the lower row, {@code false} otherwise
     */
    public boolean isCardDown(String cardId){
        return this.tribeShowed.isDown(cardId) || this.buildingShowed.isDown(cardId);
    }

    /**
     * Finds the specific offer card on the track where the given player's totem is currently placed.
     *
     * @param player the player to locate on the track
     * @return the {@link OfferCard} occupied by the player
     */
    public OfferCard findTrackPosition(Player player){
        return this.track.findTrackPosition(player);
    }

    /**
     * Initializes the board for the very first turn of the game.
     * Populates the initial rows of showed cards and randomizes the starting totem order.
     *
     * @param players the list of players participating in the game
     */
    public void setupBoard(List<Player> players){
        this.tribeShowed.addLowerCards(this.tribeDeck.drawCards(this.numPlayers + 1));
        this.displayTribeCards();
        this.displayBuildings();
        this.turnOrder.setupOrder(players);
    }

    /**
     * Concludes the current turn, triggers necessary card effects, and prepares the board for the next turn.
     * This includes shifting card rows and potentially advancing to the next era.
     *
     * @param nc            the {@link NotificatorCenter} to handle end-of-turn card effects
     * @param playerContext the context of the active player ending the turn
     */
    public void endTurn(NotificatorCenter nc, PlayerContext playerContext){
        this.tribeShowed.clearBottom();
        this.tribeShowed.shiftRow();
        this.tribeShowed.registerBottom(nc, playerContext);
        this.displayTribeCards();
        if(this.tribeShowed.diffLastEras()) this.changeEra();
    }

    /* --- PRIVATE HELPER METHODS --- */

    /**
     * Advances the game to the next era and updates the building displays accordingly.
     */
    private void changeEra(){
        this.currEra = this.currEra.next();
        this.buildingShowed.clearBottom();
        this.buildingShowed.shiftRow();
        this.displayBuildings();
    }

    /**
     * Replenishes the upper row of the tribe display by drawing from the deck.
     */
    private void displayTribeCards() {
        this.tribeShowed.addUpperCards(this.tribeDeck.drawCards(this.numPlayers + 4));
    }

    /**
     * Replenishes the upper row of the building display based on the current era's rules.
     */
    private void displayBuildings() {
        this.buildingShowed.addUpperCards(this.buildingDeck.drawCards(this.buildingsPerEra.get(this.currEra.getIndex())));
    }

    /* --- GETTERS & SNAPSHOTS --- */

    public Showed<Card> getTribeShowed() { return tribeShowed; }
    public Showed<BuildingCard> getBuildingShowed() { return buildingShowed; }
    public int getCurrEraIndex() { return currEra.getIndex(); }

    /**
     * @return the required player order for placing totems
     */
    public List <Player> getPlaceOrder(){
        return turnOrder.getPlaceOrder();
    }

    /**
     * Serializes the current state of the board into a snapshot for network transmission or persistence.
     *
     * @return a {@link BoardSnapshot} representing the current board state
     */
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
                currEra.getIndex(),
                numPlayers
        );
    }


    /**
     * Restores a previously saved board state from a snapshot and pre-loaded card mappings.
     *
     * @param snapshot the serialized state of the board
     * @param cardMap  a dictionary mapping unique card IDs to their instantiated {@link Card} objects
     * @param players  the list of active players in the game
     * @return a fully restored {@code Board} instance
     * @throws IllegalStateException if a card ID from the snapshot is missing in the provided dictionary
     */
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

        board.currEra = Era.values()[snapshot.getCurrEra()];

        return board;
    }

}
