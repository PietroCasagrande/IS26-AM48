package it.polimi.ingsw.am48.dto;

public class CardDTO {

    public String id;               //unique identifier
    public String era;              //belonging era

    public int minPlayers;          //minimum players for usage
    public String character;        //specifies character's profession
    public String eventType;        //specifies event type
    public int foodCost;            //price to be acquired
    public int prestigePoints;      //given prestige points
    public StrategyDTO strategy;    //card's effect
}
