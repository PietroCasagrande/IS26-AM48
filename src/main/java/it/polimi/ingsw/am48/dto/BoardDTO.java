package it.polimi.ingsw.am48.dto;

import java.util.List;

public class BoardDTO {
    // Board components
    public List<CardDTO> character;
    public List<CardDTO> events;
    public List<CardDTO> buildings;
    public List<OfferCardDTO> offerCards;
    public OfferTurnCardDTO offerTurnCard;
}
