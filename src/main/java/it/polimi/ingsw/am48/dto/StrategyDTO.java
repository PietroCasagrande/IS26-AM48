package it.polimi.ingsw.am48.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Data Transfer Object (DTO) used to deserialize strategy blueprints from the JSON configuration file.
 * <p>
 * This class acts as a flexible data container. To keep the JSON structure flat and avoid
 * creating a massive class with dozens of specific and null variables, it utilizes Jackson's
 * {@link JsonAlias} to dynamically map different contextual JSON properties into generic
 * numerical payload slots ({@code num1}, {@code num2}, {@code num3}).
 */
public class StrategyDTO {

    /** The exact class name or identifier of the strategy effect to instantiate. */
    public String effect;

    /** The specific notificator channel this strategy should bind to (e.g., "OnPick", "OnEvent"). */
    public String notificator;

    /** The type of artifact involved (specifically used by inventor strategies). */
    public String artifact;

    /** The type of {@code Resource} targeted or provided by the strategy. */
    public String resource;

    /** The specific character type upon which this strategy applies. */
    public String character;

    /** The specific event type that triggers this strategy's effect. */
    public String eventType;

    /**
     * Generic numerical payload slot 1.
     * <p>
     * Dynamically receives values from JSON fields such as:
     * "buildingDiscount", "quantity", "ppGained", "ppLost", "ppToMin", "threshold", or "numUp".
     */
    @JsonAlias({"buildingDiscount", "quantity", "ppGained", "ppLost", "ppToMin", "threshold", "numUp"})
    public int num1;

    /**
     * Generic numerical payload slot 2.
     * <p>
     * Dynamically receives values from JSON fields such as:
     * "builderPp", "ppToMax", "winRes", or "numDown".
     */
    @JsonAlias({"builderPp", "ppToMax", "winRes", "numDown"})
    public int num2;

    /**
     * Generic numerical payload slot 3.
     * <p>
     * Dynamically receives values from JSON fields such as:
     * "loseRes".
     */
    @JsonAlias({"loseRes"})
    public int num3;
}
