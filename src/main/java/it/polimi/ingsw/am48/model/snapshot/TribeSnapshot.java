// TribeSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TribeSnapshot implements Serializable {
    private final List<String> characterCardIds;
    private final List<String> buildingCardIds;
    private final Map<String, Integer> artifacts;
    private final int currentFood;
    private final int currentPrestigePoints;
    private final int shamanStars;
    private final int buildingDiscount;
    private final int builderPoints;
    private final int buildingPoints;

    @JsonCreator
    public TribeSnapshot(
            @JsonProperty("characterCardIds") List<String> characterCardIds,
            @JsonProperty("buildingCardIds") List<String> buildingCardIds,
            @JsonProperty("artifacts") Map<String, Integer> artifacts,
            @JsonProperty("currentFood") int currentFood,
            @JsonProperty("currentPrestigePoints") int currentPrestigePoints,
            @JsonProperty("shamanStars") int shamanStars,
            @JsonProperty("buildingDiscount") int buildingDiscount,
            @JsonProperty("builderPoints") int builderPoints,
            @JsonProperty("buildingPoints") int buildingPoints) {
        this.characterCardIds = characterCardIds;
        this.buildingCardIds = buildingCardIds;
        this.artifacts = artifacts;
        this.currentFood = currentFood;
        this.currentPrestigePoints = currentPrestigePoints;
        this.shamanStars = shamanStars;
        this.buildingDiscount = buildingDiscount;
        this.builderPoints = builderPoints;
        this.buildingPoints = buildingPoints;
    }

    public List<String> getCharacterCardIds() { return characterCardIds; }
    public List<String> getBuildingCardIds() { return buildingCardIds; }
    public Map<String, Integer> getArtifacts() { return artifacts; }
    public int getCurrentFood() { return currentFood; }
    public int getCurrentPrestigePoints() { return currentPrestigePoints; }
    public int getShamanStars() { return shamanStars; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }
}
