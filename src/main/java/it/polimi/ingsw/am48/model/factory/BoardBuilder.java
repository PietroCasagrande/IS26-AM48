package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.Deck;
import it.polimi.ingsw.am48.model.board.OfferCardTrack;
import it.polimi.ingsw.am48.model.board.OfferTurnCard;
import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.List;

/**
 * Orchestrates the complete initialization pipeline of the game board.
 * <p>
 * This builder class acts as a central facade to hide the complexity of the setup phase.
 * It manages the loading of the raw JSON game data, delegates the instantiation of
 * individual components to their respective factories, coordinates the deck building
 * via {@link DeckSetup}, and finally assembles the complete {@link Board} object.
 */
public class BoardBuilder {

    /**
     * Builds and fully initializes a new game board tailored for the specified player count.
     * <p>
     * The initialization follows a strict pipeline:
     * <ol>
     * <li><b>I/O Load:</b> Reads the base configurations from the {@code game_data.json} file into a {@link BoardDTO}.</li>
     * <li><b>Factory Generation:</b> Converts the DTO blueprints into concrete domain objects (Cards, Tracks) using specialized factories.</li>
     * <li><b>Deck Assembly:</b> Shuffles and orders the created cards into playable decks according to the game rules.</li>
     * <li><b>Board Assembly:</b> Injects all the generated components into a fresh {@link Board} instance.</li>
     * </ol>
     *
     * @param requiredPlayer the number of players participating in the game (must be valid for the game rules)
     * @return a fully configured and ready-to-play {@link Board} instance
     */
    public Board createBoard(int requiredPlayer){

        BoardDTO dto = new GameDataLoader().loadData();

        List<CharacterCard> characters = new CharacterFactory(dto.characters).createCards(requiredPlayer);
        List<EventCard> events = new EventFactory(dto.events).createCards(requiredPlayer);
        List<BuildingCard> buildings = new BuildingFactory(dto.buildings).createCards(requiredPlayer);
        OfferCardTrack track = new OfferTrackFactory(dto.offerCards).createCards(requiredPlayer).getFirst();
        OfferTurnCard offerTurnCard = new OfferTurnFactory(dto.offerTurnCard).createCards(requiredPlayer).getFirst();

        DeckSetup deckSetup = new DeckSetup(characters, events, buildings, dto.buildingSetup);
        Deck<Card> tribeDeck = new Deck<>(deckSetup.createTribeDeck(requiredPlayer));
        Deck<BuildingCard> buildingDeck = new Deck<>(deckSetup.createBuildingDeck(requiredPlayer));

        List<Integer> buildingsPerEra = dto.buildingSetup
                .get(requiredPlayer)
                .values()
                .stream()
                .toList();

        return new Board(track, offerTurnCard, tribeDeck, buildingDeck, buildingsPerEra, requiredPlayer);
    }
}
