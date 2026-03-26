package it.polimi.ingsw.am48.dto;

import java.util.Map;

public class StrategyDTO {

    public String effect;                   //strategy name
    public String artifact;                 //artifact type (inventors only)
    public String resource;                 //resource type
    public String characterType;            //character on which the strategy would be applied

    // Generic payload slots populated by StrategyDtoDeserializer based on the 'effect' string
    public int num1;
    public int num2;
    public int num3;
}
