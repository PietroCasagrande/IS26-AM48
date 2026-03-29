package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Resource;

import java.util.*;

public class Tribe {
    // final sugli oggetti per garantire che la variabile di riferimento non possa esser riassegnata a un altro oggetto
    private final Map<CharacterType, List<CharacterCard>> characters;
    private final List<BuildingCard> buildings;
    private final Set<Artifact> artifacts;
    private int currentFood;        // cibo totale tribù
    private int foodDiscount;       // totale dello sconto sul cibo dato dai picker
    private int buildingDiscount;
    private int shamanStars;
    private int builderPoints;
    private int buildingPoints;
    private int currentPrestigePoints;

    public Tribe() {
        this.characters = new EnumMap<>(CharacterType.class);
        for(CharacterType t : CharacterType.values()){
            characters.put(t, new ArrayList<>());
        }
        this.buildings = new ArrayList<>();
        this.artifacts = EnumSet.noneOf(Artifact.class);
        this.currentFood = 0;
        this.foodDiscount = 0;
        this.currentPrestigePoints = 0;
        this.shamanStars = 0;
        this.builderPoints = 0;
        this.buildingPoints = 0;
    }

    // metodi che ci servono per gestire personaggi ed edifici nella tribe
    public int tribeSize() {
        return characters.values().stream()
                .mapToInt(List::size)
                .sum();
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

    public void updateArtifacts(Artifact artifact) {
        artifacts.add(artifact);
    }

    // getter degli attributi di tribe, utilizzati nelle strategy degli eventi, per fare check sulla quantità
    public int getFoodDiscount() { return foodDiscount; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getShamanStars() { return shamanStars; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }

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
}
