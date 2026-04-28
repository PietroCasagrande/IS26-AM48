package it.polimi.ingsw.am48.model.factory;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.card.EventCard;
import it.polimi.ingsw.am48.model.enums.Era;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeckSetup {
    private final List<CharacterCard> characters;
    private final List<EventCard> events;
    private final List<BuildingCard> buildings;
    private final Map<Integer, Map<Era, Integer>> buildingSetup;

    public  DeckSetup(List<CharacterCard> characters, List<EventCard> events, List<BuildingCard> buildings,
                      Map<Integer, Map<Era, Integer>> buildingSetup) {
        this.characters = new ArrayList<>(characters);
        this.events = new ArrayList<>(events);
        this.buildings = new ArrayList<>(buildings);
        this.buildingSetup = buildingSetup;
    }

    public List<Card> createTribeDeck(int numPlayers){
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");

        // Shuffle character cards and
        Collections.shuffle(characters);

        // Order per Era and set first numPlayers + 1 cards on top of the tribe deck (for lowerRow)
        List<CharacterCard> onTopCards = new ArrayList<>( this.characters.stream()
                .sorted((c1, c2) -> Integer.compare(c1.getEra().getIndex(), c2.getEra().getIndex()))
                .limit(numPlayers + 1)
                .toList());
        this.characters.removeAll(onTopCards);
        List<Card> tribeDeck = new ArrayList<>(onTopCards);

        // Extract final events (last two in event list, guaranteed by json file)
        List<Card> finalEvents = new ArrayList<>(this.events.subList(this.events.size() - 2, this.events.size()));
        this.events.subList(this.events.size() - 2, this.events.size()).clear();

        // Mix characters and events
        List<Card> mixTribe = new ArrayList<>(this.characters);
        mixTribe.addAll(this.events);
        Collections.shuffle(mixTribe);

        // Order per Era and add to tribe deck
        tribeDeck.addAll(mixTribe.stream()
                .sorted((c1, c2) -> Integer.compare(c1.getEra().getIndex(), c2.getEra().getIndex()))
                .toList());

        // Add final events
        tribeDeck.addAll(finalEvents);

        return tribeDeck;
    }

    public List<BuildingCard> createBuildingDeck(int numPlayers){
        if(numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException("Invalid number of players");
        Collections.shuffle(buildings);
        List<BuildingCard> buildingDeck = new ArrayList<>();

        for(Era era : Era.values()){
            buildingDeck.addAll(this.buildings.stream()
                    .filter(b -> b.getEra().equals(era))
                    .limit(this.buildingSetup.get(numPlayers).get(era))
                    .toList());
        }
        return buildingDeck;
    }
}
