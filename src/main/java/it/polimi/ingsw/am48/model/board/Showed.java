package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.notificator.NotificatorCenter;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.*;

/**
 * Manages the display and interaction of a dual-row card offering (upper and lower rows).
 * <p>
 * This generic class is designed to handle different types of cards (Character + Events cards, and
 * Building cards) that are laid out on the board in two distinct rows. It provides mechanisms
 * to add, retrieve, shift, and register card effects based on their position.
 *
 * @param <T> the specific type of {@link Card} managed by this display
 */
public class Showed<T extends Card>{
    private final List<T> upperList;
    private final List<T> lowerList;

    /**
     * Creates a new empty {@code Showed} display, initializing both the upper and lower rows.
     */
    public Showed(){
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
    }

    /**
     * Adds a batch of new cards to the upper row.
     * Called at each round when the deck replenishes the board.
     *
     * @param cardsList the list of cards to be appended to the upper row
     */
    public void addUpperCards(List<T> cardsList){
        upperList.addAll(cardsList);
    }

    /**
     * Adds a batch of new cards directly to the lower row.
     * Performed only during the initial setup of the game.
     *
     * @param cardsList the list of cards to be appended to the lower row
     */
    public void addLowerCards(List<T> cardsList){
        lowerList.addAll(cardsList);
    }

    /**
     * Moves all cards currently in the upper row down to the lower row, clearing the upper row.
     * <p>
     * The frequency of this shift depends on the specific game rules for the card type
     * (e.g., Character cards shift every turn, Building cards shift only at the end of an era).
     */
    public void shiftRow(){
        lowerList.addAll(upperList);
        upperList.clear();
    }

    /**
     * Registers the triggerable effects (Strategies) of all cards in the lower row
     * to the provided notification center. Cards are processed sorted by their era index.
     * <p>
     * Note: If a specific card type provided by {@code T} does not possess an active
     * effect, its Strategy should gracefully handle the registration (e.g., by being
     * empty or null) without disrupting the execution flow.
     *
     * @param nc the {@link NotificatorCenter} handling the observer pattern
     * @param playerContext the context of the player triggering these events
     */
    public void registerBottom(NotificatorCenter nc, PlayerContext playerContext){
        List<T> sortedCards = this.lowerList.stream()
                .sorted(Comparator.comparingInt(c -> c.getEra().getIndex()))
                .toList();
        for(T card : sortedCards)
            if (card.getStrategy() != null)
                card.getStrategy().registerTo(nc, playerContext);
    }

    /**
     * Registers the triggerable effects (Strategies) of all cards in the upper row
     * to the provided notification center. Typically used only during the final turn.
     * Cards are processed sorted by their era index.
     *
     * @param nc the {@link NotificatorCenter} handling the observer pattern
     * @param playerContext the context of the player triggering these events
     */
    public void registerUpper(NotificatorCenter nc, PlayerContext playerContext){
        List<T> sortedCards = this.upperList.stream()
                .sorted(Comparator.comparingInt(c -> c.getEra().getIndex()))
                .toList();
        for(T card : sortedCards)
            if (card.getStrategy() != null)
                card.getStrategy().registerTo(nc, playerContext);
    }

    /**
     * Discards all cards currently present in the lower row.
     */
    public void clearBottom(){
        lowerList.clear();
    }

    /**
     * Evaluates if there is an era mismatch between the most recently added card
     * in the upper row and the most recently added card in the lower row.
     *
     * @return {@code true} if both rows are populated and their last cards belong to different eras, {@code false} otherwise
     */
    public boolean diffLastEras(){
        if(upperList.isEmpty() || lowerList.isEmpty()) return false;
        return upperList.getLast().getEra() != lowerList.getLast().getEra();
    }

    /**
     * Attempts to locate and acquire a card by its ID from the display.
     * The method searches the upper row first, and then the lower row.
     * If found, the card is acquired by the player and removed from the board.
     *
     * @param playerContext the context of the player attempting to acquire the card
     * @param cardId        the unique identifier of the desired card
     * @return an {@link Optional} containing the acquired card if found, or an empty {@code Optional} otherwise
     */
    public Optional<T> takeCard(PlayerContext playerContext, String cardId){

        Iterator<T> upperIt = upperList.iterator();
        while(upperIt.hasNext()){
            T card = upperIt.next();
            if(card.getCardId().equals(cardId)){
                card.acquire(playerContext);
                upperIt.remove();
                return Optional.of(card);
            }
        }

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

    /**
     * Checks whether a card with the specified ID is currently located in the upper row.
     *
     * @param cardId the unique identifier of the card to check
     * @return {@code true} if the card is in the upper row, {@code false} otherwise
     */
    public boolean isTop(String cardId){
        return upperList.stream().anyMatch(c -> c.getCardId().equals(cardId));
    }

    /**
     * Checks whether a card with the specified ID is currently located in the lower row.
     *
     * @param cardId the unique identifier of the card to check
     * @return {@code true} if the card is in the lower row, {@code false} otherwise
     */
    public boolean isDown(String cardId){
        return lowerList.stream().anyMatch(c -> c.getCardId().equals(cardId));
    }

    // getters for board

    public List<T> getUpperList() {
        return List.copyOf(upperList);
    }

    public List<T> getLowerList() {
        return List.copyOf(lowerList);
    }
}
