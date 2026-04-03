package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.snapshot.TribeSnapshot;

import java.util.*;
import java.util.stream.Collectors;

public class Tribe {
    // final sugli oggetti per garantire che la variabile di riferimento non possa esser riassegnata a un altro oggetto
    private final Map<CharacterType, List<CharacterCard>> characters;
    private final List<BuildingCard> buildings;
    private final Map<Artifact, Integer> artifacts;
    private int currentFood;        // cibo totale tribù
    private int foodDiscount;       // totale dello sconto sul cibo dato dai picker
    private int buildingDiscount;
    private int shamanStars;
    private boolean shamanSafety;    // indica se il Player è immune all'evento sciamanico
    private boolean shamanDoubling;
    private int builderPoints;
    private int buildingPoints;
    private int currentPrestigePoints;

    public Tribe() {
        this.characters = new EnumMap<>(CharacterType.class);
        for(CharacterType t : CharacterType.values()){
            characters.put(t, new ArrayList<>());
        }
        this.buildings = new ArrayList<>();
        this.artifacts = new EnumMap<>(Artifact.class);
        this.currentFood = 0;
        this.foodDiscount = 0;
        this.currentPrestigePoints = 0;
        this.shamanStars = 0;
        this.shamanSafety = false;
        this.shamanDoubling = false;
        this.builderPoints = 0;
        this.buildingPoints = 0;
    }

    public int countByType(CharacterType type) {
        return characters.get(type).size();
    }

    public void addToTribe(CharacterCard card) {
        characters.get(card.getType()).add(card);
    }

    public void addToTribe(BuildingCard card) {
        buildings.add(card);
        this.buildingPoints += card.getPrestigePoints();
    }

    // setter utilizzati nelle strategy, in particolare UpdateResources, per aggiornare le risorse della tribe
    public void updateFoodDiscount(int amount) {
        this.foodDiscount += amount;
    }

    public void updateBuildingDiscount(int amount) {
        this.buildingDiscount += amount;
    }

    public void updateShamanStars(int amount) {
        this.shamanStars += amount;
    }

    public void updateBuilderPoints(int amount) {
        this.builderPoints += amount;
    }

    public void updateBuildingPoints(int amount) {
        this.buildingPoints += amount;
    }

    public void addArtifact(Artifact artifact) {
        artifacts.merge(artifact, 1, Integer::sum);
    }

    public void setShamanSafety() { this.shamanSafety = true; }

    public void setShamanDoubling() { this.shamanDoubling = true; }

    // getter degli attributi di tribe, utilizzati nelle strategy degli eventi, per fare check sulla quantità
    public int getFoodDiscount() { return foodDiscount; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getShamanStars() { return shamanStars; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }
    public boolean isShamanSafe() { return shamanSafety; }
    public boolean deservesDoubleShamanPp() { return shamanDoubling; }

    // sostituisce il vecchio tribeSize(), getTotalCharacters() in player delega a questo
    public int getTotalCharacters(){
        return characters.values().stream()
                 .mapToInt(List::size)
                 .sum();
    }

    // metodi per statistiche del giocatore, ovvero punti e cibo
    public int getCurrentFood() { return currentFood; }
    public void updateCurrentFood(int currentFood) { this.currentFood += currentFood; }
    public int getCurrentPrestigePoints() { return currentPrestigePoints; }
    public void updateCurrentPrestigePoints(int currentPrestigePoints) { this.currentPrestigePoints += currentPrestigePoints; }

    // metodo per pagare cibo e perdere punti in caso di cibo insufficiente
    // food e ppPerFood devono essere positivi
    public void payFood(int food, int ppPerFood){
        if(this.currentFood - food >= 0) this.currentFood -= food;
        else {
            this.currentPrestigePoints += (this.currentFood - food) * ppPerFood;
            this.currentFood = 0;
        }
    }

    // metodo che restituisce numero di personaggi la cui occorrenza di CharacterType è presente in minore quantità all'interno della mappa characters (anche 0 se nessuno di quel tipo)
    public int minListSize() {
        if (characters.size() < CharacterType.values().length) return 0;
        return characters.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }

    // conta il numero di coppie di inventori con lo stesso Artifact
    public int countInventorPairs() {
        return artifacts.values().stream()
                .mapToInt(count -> count / 2)
                .sum();
    }

    // metodo utilizzato per il calcolo dei punti finali della tribù: currentPrestigePoints + puntiInventori + puntiPicker
    public void computeTotalEndGameScore() {
        // punti edifici (accumulati all'acquisto degli edifici, tramite addToTribe(BuildingCard))
        this.currentPrestigePoints += builderPoints;
        // punti costruttori (accumulati alla selezione di un personaggio costruttore)
        this.currentPrestigePoints += buildingPoints;

        // punti artisti: 10PP per ogni coppia di artisti
        int artistsPair = countByType(CharacterType.ARTIST) / 2;
        this.currentPrestigePoints += artistsPair * 10;

        // punti inventori: numero di inventori * artefatti diversi
        int numOfInventors = countByType(CharacterType.INVENTOR);
        int numOfArtifacts = artifacts.size();
        this.currentPrestigePoints += numOfInventors * numOfArtifacts;
    }

    // getter per i test di TribeTest e per le strategy
    public Map<Artifact, Integer> getArtifacts() { return Collections.unmodifiableMap(artifacts); }
    public List<BuildingCard> getBuildings() { return Collections.unmodifiableList(buildings); }
    public Map<CharacterType, List<CharacterCard>> getCharacters() { return Collections.unmodifiableMap(characters); }

    public TribeSnapshot toSnapshot() {
        List<String> charactersIds = characters.values().stream()
                .flatMap(List::stream)
                .map(Card::getCardId)
                .toList();

        List<String> buildingIds = buildings.stream()
                .map(Card::getCardId)
                .toList();

        Map<String, Integer> artifactSnapshot = artifacts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), Map.Entry::getValue
                ));

        return new TribeSnapshot(
                charactersIds,
                buildingIds,
                artifactSnapshot,
                currentFood,
                currentPrestigePoints,
                shamanStars,
                buildingDiscount,
                builderPoints,
                buildingPoints
        );
    }
}
