package it.polimi.ingsw.am48.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class StrategyDTO {

    public String effect;                   //strategy name
    public String notificator;              //notificator type
    public String artifact;                 //artifact type (inventors only)
    public String resource;                 //resource type
    public String character;                //character on which the strategy would be applied
    public String eventType;                //event on which building has effect

    // Generic payload slots
    @JsonAlias({"buildingDiscount", "quantity", "ppGained", "ppLost", "ppToMin", "threshold", "numUp"})
    public int num1;

    @JsonAlias({"builderPp", "ppToMax", "winRes", "numDown"})
    public int num2;

    @JsonAlias({"loseRes"})
    public int num3;
}
