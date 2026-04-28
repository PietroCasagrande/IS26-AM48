// TribeSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import java.util.List;
import java.util.Map;

public class TribeSnapshot {
    private final List<String> characterCardIds;
    private final List<String> buildingCardIds;
    private final Map<String, Integer> artifacts;
    private final int food;
    private final int currentPrestigePoints;
    private final int shamanStars;
    private final int buildingDiscount;
    private final int builderPoints;
    private final int buildingPoints;

    public TribeSnapshot(List<String> characterCardIds, List<String> buildingCardIds,
                         Map<String, Integer> artifacts, int food, int currentPrestigePoints,
                         int shamanStars, int buildingDiscount, int builderPoints,
                         int buildingPoints) {
        this.characterCardIds = characterCardIds;
        this.buildingCardIds = buildingCardIds;
        this.artifacts = artifacts;
        this.food = food;
        this.currentPrestigePoints = currentPrestigePoints;
        this.shamanStars = shamanStars;
        this.buildingDiscount = buildingDiscount;
        this.builderPoints = builderPoints;
        this.buildingPoints = buildingPoints;
    }

    public List<String> getCharacterCardIds() { return characterCardIds; }
    public List<String> getBuildingCardIds() { return buildingCardIds; }
    public Map<String, Integer> getArtifacts() { return artifacts; }
    public int getFood() { return food; }
    public int getCurrentPrestigePoints() { return currentPrestigePoints; }
    public int getShamanStars() { return shamanStars; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }
}
