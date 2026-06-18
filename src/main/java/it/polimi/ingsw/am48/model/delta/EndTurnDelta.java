// EndTurnDelta.java
package it.polimi.ingsw.am48.model.delta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

/**
 * {@link GameDelta} broadcast at the end of a turn. It carries the updated food
 * and prestige points of every player, the refreshed "showed" tribe and
 * building rows, the new turn, phase and era, plus the list of
 * {@link EventInfo events} triggered during the turn.
 *
 * @see GameDelta
 * @see EventInfo
 * @see ClientModel
 */
public class EndTurnDelta extends GameDelta {
    private final Map<String, Integer> updatedFood;        // nickname -> cibo aggiornato
    private final Map<String, Integer> updatedPrestige;    // nickname -> PP aggiornati
    private final List<String> newUpperTribeIds;             // nuove carte personaggio ed evento nella fila superiore
    private final List<String> newLowerTribeIds;             // carte spostate nella fila inferiore
    private final List<String> newUpperBuildingIds;     // nuovi edifici (se cambio era)
    private final List<String> newLowerBuildingIds;
    private final int currentTurn;
    private final String currentPhase;
    private final List<EventInfo> events;
    private final int currentEra;

    /**
     * Creates a new end-of-turn delta.
     *
     * @param updatedFood map from each player's nickname to their updated food
     * @param updatedPrestige map from each player's nickname to their updated
     *                        prestige points
     * @param newUpperTribeIds the new upper tribe row (new character and event
     *                         cards)
     * @param newLowerTribeIds the cards moved into the lower tribe row
     * @param newUpperBuildingIds the new upper building row (e.g. on era change)
     * @param newLowerBuildingIds the new lower building row
     * @param currentTurn the index of the new current turn
     * @param currentPhase the game phase after applying this delta
     * @param events the events triggered during the turn; may be {@code null},
     *               in which case it is treated as an empty list
     * @param currentEra the index of the current era
     */
    @JsonCreator
    public EndTurnDelta(
            @JsonProperty("updatedFood")Map<String, Integer> updatedFood,
            @JsonProperty("updatedPrestige")Map<String, Integer> updatedPrestige,
            @JsonProperty("newUpperTribeIds")List<String> newUpperTribeIds,
            @JsonProperty("newLowerTribeIds")List<String> newLowerTribeIds,
            @JsonProperty("newUpperBuildingIds")List<String> newUpperBuildingIds,
            @JsonProperty("newLowerBuildingIds")List<String> newLowerBuildingIds,
            @JsonProperty("currentTurn") int currentTurn,
            @JsonProperty("currentPhase") String currentPhase,
            @JsonProperty("events") List<EventInfo> events,
            @JsonProperty("currentEra") int currentEra) {
        this.updatedFood = updatedFood;
        this.updatedPrestige = updatedPrestige;
        this.newUpperTribeIds = newUpperTribeIds;
        this.newLowerTribeIds = newLowerTribeIds;
        this.newUpperBuildingIds = newUpperBuildingIds;
        this.newLowerBuildingIds = newLowerBuildingIds;
        this.currentTurn = currentTurn;
        this.currentPhase = currentPhase;
        this.events = events != null ? events : new ArrayList<>();
        this.currentEra = currentEra;
    }

    /**
     * Applies the end-of-turn update to the client model: refreshes the food
     * and prestige points of every player, updates the showed tribe and
     * building rows, advances the turn, phase and era, and registers any
     * events triggered during the turn.
     *
     * @param model the client model whose state must be updated
     */
    // EndTurn modifica showed e food e pp di tutti i giocatori
    @Override
    public void applyTo(ClientModel model) {
        model.setPhase("EndTurnPhase");
        updatedFood.keySet().forEach(nick -> {
            model.updatePlayerFood(nick, updatedFood.get(nick));
        });

        updatedPrestige.keySet().forEach(nick -> {
            model.updatePlayerPoints(nick, updatedPrestige.get(nick));
        });
        model.updateTribeShowed(newUpperTribeIds, newLowerTribeIds);
        model.updateBuildingShowed(newUpperBuildingIds, newLowerBuildingIds);

        model.setPhase(currentPhase);
        model.incrementTurn(currentTurn);

        if (!events.isEmpty()) {
            model.setEvents(events);
        }
        model.setEra(currentEra);
    }

    // getter per tutti i campi
    public Map<String, Integer> getUpdatedFood() { return updatedFood; }
    public Map<String, Integer> getUpdatedPrestige() { return updatedPrestige; }
    public List<String> getNewUpperTribeIds() { return newUpperTribeIds; }
    public List<String> getNewLowerTribeIds() { return newLowerTribeIds; }
    public List<String> getNewUpperBuildingIds() { return newUpperBuildingIds; }
    public List<String> getNewLowerBuildingIds() { return newLowerBuildingIds; }
    public int getCurrentTurn() { return currentTurn; }
    public String getCurrentPhase() { return currentPhase; }
    public List<EventInfo> getEvents() { return events; }
    public int getCurrentEra() { return currentEra; }
}
