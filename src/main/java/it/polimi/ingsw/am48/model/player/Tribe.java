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
    private int food;
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
        this.food = 0;
        this.currentPrestigePoints = 0;
        this.shamanStars = 0;
        this.builderPoints = 0;
        this.buildingPoints = 0;
    }

    public int tribeSize() { throw new UnsupportedOperationException("TODO"); }
    public int countByType(CharacterType type) { throw new UnsupportedOperationException("TODO"); }
    public void addToTribe(CharacterCard card) { throw new UnsupportedOperationException("TODO"); }
    public void addToTribe(BuildingCard card) { throw new UnsupportedOperationException("TODO"); }
    public void updateResource(Resource resource, int amount) { throw new UnsupportedOperationException("TODO"); }
    public void updateArtifact(Artifact artifact) { throw new UnsupportedOperationException("TODO"); }
    public void computeTotalEndGameScore() { throw new UnsupportedOperationException("TODO"); }

    // getters per punti e cibo, da capire se servono effettivamente
    public int getFood() { return food; }
    public int getCurrentPrestigePoints() { return currentPrestigePoints; }
}
