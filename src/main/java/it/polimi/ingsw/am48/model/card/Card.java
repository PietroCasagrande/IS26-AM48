package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

/**
 * The abstract base class representing a generic playable card in the game.
 * <p>
 * This class establishes the fundamental contract for all specific card types
 * (such as Characters, Events, and Buildings). It encapsulates the core identity
 * of a card, its chronological placement within the game's eras, and its unique
 * behavior defined through the Strategy pattern.
 */
public abstract class Card {
    protected final String cardId;
    protected final Era era;

    /**
     * The specific effect or behavior associated with this card.
     * Can be null if the card has no active abilities.
     */
    protected final CardStrategy strategy;

    /**
     * Constructs a new base {@code Card} with its fundamental attributes.
     * <p>
     * This constructor is protected as it is meant to be invoked exclusively
     * by concrete subclasses (BuildingCard, CharacterCard, EventCard) during instantiation.
     *
     * @param cardId   the unique string identifier for this card
     * @param era      the chronological era to which this card belongs
     * @param strategy the specific effect or behavior encapsulated by this card
     */
    protected Card(String cardId, Era era, CardStrategy strategy){
        this.cardId = cardId;
        this.era = era;
        this.strategy = strategy;
    }

    /**
     * Executes the specific domain logic required when a player successfully acquires this card.
     * <p>
     * Concrete subclasses must implement this method to define their unique acquisition
     * mechanics. Only Characters and Buildings can be acquired. This typically includes
     * verifying and deducting resource costs, placing the card on the player's personal board,
     * or immediately triggering "one-shot" effects. This approach follows the "Tell, Don't Ask" Principle.
     *
     * @param playerContext the context and state of the player who is acquiring the card
     */
    public abstract void acquire(PlayerContext playerContext);

    public String getCardId() { return cardId; }
    public Era getEra() { return era; }
    public CardStrategy getStrategy() { return strategy; }

}
