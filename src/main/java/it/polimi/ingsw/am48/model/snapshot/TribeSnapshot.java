// TribeSnapshot.java
package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Serializable, immutable snapshot of a player's tribe. It captures the ids of
 * the owned character and building cards, the collected artifacts and all the
 * scalar resources and bonuses (food, prestige points, shaman stars, discounts
 * and the various rights/flags) that make up the tribe's state.
 *
 * <p>Cards are stored as ids and resolved back to {@code Card} instances when
 * the tribe is restored from the snapshot.
 * </p>
 *
 * @see PlayerSnapshot
 */
public class TribeSnapshot implements Serializable {
    private final List<String> characterCardIds;
    private final List<String> buildingCardIds;
    private final Map<String, Integer> artifacts;
    private final int currentFood;
    private final int currentPrestigePoints;
    private final int shamanStars;
    private final int foodDiscount;
    private final boolean shamanSafety;    // indica se il Player è immune all'evento sciamanico
    private final boolean shamanDoubling;
    private final boolean extraFoodRight;
    private final boolean extraPickRight;
    private final int buildingDiscount;
    private final int builderPoints;
    private final int buildingPoints;

    /**
     * Creates a new tribe snapshot.
     *
     * @param characterCardIds ids of the owned character cards
     * @param buildingCardIds ids of the owned building cards
     * @param artifacts map from artifact name to the number owned
     * @param currentFood the current amount of food
     * @param currentPrestigePoints the current prestige points
     * @param shamanStars the number of shaman stars
     * @param foodDiscount the current food discount
     * @param shamanSafety {@code true} if the player is immune to the shaman
     *                     event
     * @param shamanDoubling {@code true} if the shaman doubling effect is active
     * @param extraFoodRight {@code true} if the player holds an extra-food right
     * @param extraPickRight {@code true} if the player holds an extra-pick right
     * @param buildingDiscount the current building discount
     * @param builderPoints the points granted by the builder bonus
     * @param buildingPoints the points granted by owned buildings
     */
    @JsonCreator
    public TribeSnapshot(
            @JsonProperty("characterCardIds") List<String> characterCardIds,
            @JsonProperty("buildingCardIds") List<String> buildingCardIds,
            @JsonProperty("artifacts") Map<String, Integer> artifacts,
            @JsonProperty("currentFood") int currentFood,
            @JsonProperty("currentPrestigePoints") int currentPrestigePoints,
            @JsonProperty("shamanStars") int shamanStars,
            @JsonProperty("foodDiscount") int foodDiscount,
            @JsonProperty("shamanSafety") boolean shamanSafety,
            @JsonProperty("ShamanDoubling") boolean shamanDoubling,
            @JsonProperty("extraFoodRight") boolean extraFoodRight,
            @JsonProperty("extraPickRight") boolean extraPickRight,
            @JsonProperty("buildingDiscount") int buildingDiscount,
            @JsonProperty("builderPoints") int builderPoints,
            @JsonProperty("buildingPoints") int buildingPoints) {
        this.characterCardIds = characterCardIds;
        this.buildingCardIds = buildingCardIds;
        this.artifacts = artifacts;
        this.currentFood = currentFood;
        this.currentPrestigePoints = currentPrestigePoints;
        this.shamanStars = shamanStars;
        this.foodDiscount = foodDiscount;
        this.shamanSafety = shamanSafety;
        this.shamanDoubling = shamanDoubling;
        this.extraFoodRight = extraFoodRight;
        this.extraPickRight = extraPickRight;
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
    public int getFoodDiscount() { return foodDiscount; }
    public boolean getShamanSafety() { return shamanSafety; }
    public boolean getShamanDoubling() { return shamanDoubling; }
    public boolean getExtraFoodRight() { return extraFoodRight; }
    public boolean getExtraPickRight() { return extraPickRight; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }
}
