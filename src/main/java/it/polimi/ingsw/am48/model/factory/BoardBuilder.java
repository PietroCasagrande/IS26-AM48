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

public class BoardBuilder {
    public Board createBoard(int requiredPlayer){
        // Read game_data.json and create dto
        BoardDTO dto = new GameDataLoader().loadData();

        // Factory pattern convert dto to objects
        List<CharacterCard> characters = new CharacterFactory(dto.characters).createCards(requiredPlayer);
        List<EventCard> events = new EventFactory(dto.events).createCards(requiredPlayer);
        List<BuildingCard> buildings = new BuildingFactory(dto.buildings).createCards(requiredPlayer);
        OfferCardTrack track = new OfferTrackFactory(dto.offerCards).createCards(requiredPlayer).getFirst();
        OfferTurnCard offerTurnCard = new OfferTurnFactory(dto.offerTurnCard).createCards(requiredPlayer).getFirst();

        //Tribe and Building decks generation
        DeckSetup deckSetup = new DeckSetup(characters, events, buildings, dto.buildingSetup);
        Deck<Card> tribeDeck = new Deck<>(deckSetup.createTribeDeck(requiredPlayer));
        Deck<BuildingCard> buildingDeck = new Deck<>(deckSetup.createBuildingDeck(requiredPlayer));

        //Number of buildings to display per era
        List<Integer> buildingsPerEra = dto.buildingSetup
                .get(requiredPlayer)
                .values()
                .stream()
                .toList();

        //Board creation
        return new Board(track, offerTurnCard, tribeDeck, buildingDeck, buildingsPerEra, requiredPlayer);
    }
}
