package it.polimi.ingsw.am48.dto;

import it.polimi.ingsw.am48.model.enums.Era;

import java.util.List;
import java.util.Map;

/**
 * The root Data Transfer Object (DTO) for the game's initial configuration.
 * <p>
 * This class maps directly to the root structure of the {@code game_data.json} file.
 * It acts as the primary container loaded by the data loader, aggregating all the
 * raw blueprints required by the factories to instantiate the complete game board.
 */
public class BoardDTO {

    /** The complete list of blueprints for Character cards. */
    public List<CardDTO> characters;

    /** The complete list of blueprints for Event cards. */
    public List<CardDTO> events;

    /** The complete list of blueprints for Building cards. */
    public List<CardDTO> buildings;

    /** The complete list of blueprints representing individual slots on the offer track. */
    public List<OfferCardDTO> offerCards;

    /** The complete list of configurations for resolving the end of the offer phase. */
    public List<OfferTurnCardDTO> offerTurnCard;

    /**
     * The configuration matrix defining the setup of the building deck.
     * <p>
     * The outer map uses the <b>number of players</b> as the key.
     * The inner map associates an <b>Era</b> to the exact <b>number of building cards</b>
     * that must be drawn and displayed for that specific era.
     */
    public Map<Integer, Map<Era, Integer>> buildingSetup;
}