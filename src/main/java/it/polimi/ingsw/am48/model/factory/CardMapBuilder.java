package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.HashMap;
import java.util.Map;

public class CardMapBuilder {
    // costruisce mappa di tutte le carte in gioco come dizionario da cui poter pescare quando bisogna riassegnarle alle tribe dei player dopo il crash del server (metodo Game.fromSnapshot(GameSnapshot))
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
