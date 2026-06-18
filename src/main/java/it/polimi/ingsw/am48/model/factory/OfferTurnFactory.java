package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.OfferTurnCardDTO;
import it.polimi.ingsw.am48.model.board.OfferTurnCard;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete factory responsible for instantiating the {@link OfferTurnCard}.
 * <p>
 * This factory parses a list of data transfer objects (DTOs) containing different
 * configurations for the offer turn mechanics. It filters these configurations
 * to extract and build only the setup that exactly matches the current game's player count.
 */
public class OfferTurnFactory implements BoardFactory<OfferTurnCard> {
    private final List<OfferTurnCardDTO> offerTurn;

    /**
     * Creates a new {@code OfferTurnFactory} initialized with the available turn configurations.
     *
     * @param offerTurn a list of {@link OfferTurnCardDTO} containing the blueprints for various player counts
     */
    public OfferTurnFactory(List<OfferTurnCardDTO> offerTurn) { this.offerTurn = offerTurn; }

    /**
     * Generates a list containing only the {@link OfferTurnCard} configured for the specified
     * number of players.
     * <p>
     * The factory iterates through the available blueprints and selects only the one
     * where the required number of players strictly matches the provided {@code numPlayers}.
     * It is returned through a list to apply polymorphism and respect the contract of the
     * BoardFactory interface ensuring reusability.
     *
     * @param numPlayers the number of players in the game (must be between 2 and 5)
     * @return a list containing the initialized {@link OfferTurnCard} for the current setup
     * @throws IllegalArgumentException if the number of players is outside the valid range (2-5)
     */
    @Override
    public List<OfferTurnCard> createCards(int numPlayers){
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        List<OfferTurnCard> cards = new ArrayList<>();

        for(OfferTurnCardDTO dto : this.offerTurn){
            if(dto.numPlayers == numPlayers){
                cards.add(new OfferTurnCard(dto.numPlayers, dto.foodRewards, dto.ppPenalty));
            }
        }

        return cards;
    }
}
