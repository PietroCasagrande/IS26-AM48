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
    public int tribeSize() { throw new UnsupportedOperationException("TODO"); }
    public int countByType(CharacterType type) { throw new UnsupportedOperationException("TODO"); }
    public void addToTribe(CharacterCard card) { throw new UnsupportedOperationException("TODO"); }
    public void addToTribe(BuildingCard card) { throw new UnsupportedOperationException("TODO"); }

    // setter utilizzati nelle strategy, in particolare UpdateResources, per aggiornare le risorse della tribe
    public void updateFoodDiscount(int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateBuildingDiscount(int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateShamanStars(int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateBuilderPoints(int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateBuildingPoints(int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateArtifacts(Artifact artifact) { throw new UnsupportedOperationException("TODO"); }

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
    // ppPerFood deve essere negativo
    public void payFood(int food, int ppPerFood){
        if(this.currentFood - food >= 0) this.currentFood -= food;
        else {
            this.currentFood = 0;
            this.currentPrestigePoints += (food - this.currentFood) * ppPerFood;
        }
    }

    // metodo utilizzato per il calcolo dei punti finali della tribù: currentPrestigePoints + puntiInventori + puntiPicker
    public void computeTotalEndGameScore() { throw new UnsupportedOperationException("TODO"); }
}
