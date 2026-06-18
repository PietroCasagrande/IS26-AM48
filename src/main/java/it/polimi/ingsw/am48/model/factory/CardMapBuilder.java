package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility builder responsible for generating a global registry of all active game cards.
 * <p>
 * This class creates a comprehensive dictionary mapping each card's unique identifier
 * to its corresponding instantiated {@link Card} object. This registry is primarily
 * utilized during the server persistence recovery phase (e.g., restoring a game from
 * a snapshot after a crash) to efficiently fetch and reassign the exact card references
 * to players' tribes, decks, and the board.
 */
public class CardMapBuilder {

    /**
     * Builds and populates the complete dictionary of cards tailored for the specified player count.
     * <p>
     * The method loads the base JSON configuration data, invokes the respective factories
     * (Characters, Buildings, Events) to instantiate only the cards valid for the given
     * player count, and aggregates them into a single lookup map.
     *
     * @param numPlayers the number of players in the game, used to filter out invalid cards
     * @return a map associating each unique card ID (String) to its instantiated {@link Card} object
     */
    public static Map<String, Card> buildCardMap(int numPlayers) {
        BoardDTO dto = new GameDataLoader().loadData();
        Map<String, Card> map = new HashMap<>();

        new CharacterFactory(dto.characters).createCards(numPlayers)
                .forEach(c -> map.put(c.getCardId(), c));
        new BuildingFactory(dto.buildings).createCards(numPlayers)
                .forEach(c -> map.put(c.getCardId(), c));

        new EventFactory(dto.events).createCards(numPlayers)
                .forEach(c -> map.put(c.getCardId(), c));

        return map;
    }
}
