// EndTurnDelta.java
package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.am48.network.client.ClientModel;

public class EndTurnDelta extends GameDelta {
    private final Map<String, Integer> updatedFood;        // nickname -> cibo aggiornato
    private final Map<String, Integer> updatedPrestige;    // nickname -> PP aggiornati
    private final List<String> newUpperTribeIds;             // nuove carte personaggio ed evento nella fila superiore
    private final List<String> newLowerTribeIds;             // carte spostate nella fila inferiore
    private final List<String> newUpperBuildingIds;     // nuovi edifici (se cambio era)
    private final List<String> newLowerBuildingIds;

    @JsonCreator
    public EndTurnDelta(
            @JsonProperty("updatedFood")Map<String, Integer> updatedFood,
            @JsonProperty("updatedPrestige")Map<String, Integer> updatedPrestige,
            @JsonProperty("newUpperTribeIds")List<String> newUpperTribeIds,
            @JsonProperty("newLowerTribeIds")List<String> newLowerTribeIds,
            @JsonProperty("newUpperBuildingIds")List<String> newUpperBuildingIds,
            @JsonProperty("newLowerBuildingIds")List<String> newLowerBuildingIds) {
        this.updatedFood = updatedFood;
        this.updatedPrestige = updatedPrestige;
        this.newUpperTribeIds = newUpperTribeIds;
        this.newLowerTribeIds = newLowerTribeIds;
        this.newUpperBuildingIds = newUpperBuildingIds;
        this.newLowerBuildingIds = newLowerBuildingIds;
    }

    // EndTurn modifica showed e food e pp di tutti i giocatori
    @Override
    public void applyTo(ClientModel model) {
        updatedFood.keySet().forEach(nick -> {
            model.updatePlayerFood(nick, updatedFood.get(nick));
        });

        updatedPrestige.keySet().forEach(nick -> {
            model.updatePlayerPoints(nick, updatedPrestige.get(nick));
        });

        model.updateTribeShowed(newUpperTribeIds, newLowerTribeIds);
        model.updateBuildingShowed(newUpperBuildingIds, newLowerBuildingIds);
    }

    // getter per tutti i campi
    public Map<String, Integer> getUpdatedFood() { return updatedFood; }
    public Map<String, Integer> getUpdatedPrestige() { return updatedPrestige; }
    public List<String> getNewUpperTribeIds() { return newUpperTribeIds; }
    public List<String> getNewLowerTribeIds() { return newLowerTribeIds; }
    public List<String> getNewUpperBuildingIds() { return newUpperBuildingIds; }
    public List<String> getNewLowerBuildingIds() { return newLowerBuildingIds; }
}
