package it.polimi.ingsw.am48.dto;

import it.polimi.ingsw.am48.model.enums.Era;

import java.util.List;
import java.util.Map;

public class BoardDTO {
    // Board components
    public List<CardDTO> characters;
    public List<CardDTO> events;
    public List<CardDTO> buildings;
    public List<OfferCardDTO> offerCards;
    public OfferTurnCardDTO offerTurnCard;
    public Map<Integer, Map<Era, Integer>> buildingSetup;
}
