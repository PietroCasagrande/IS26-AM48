// EndTurnDelta.java
package it.polimi.ingsw.am48.model.delta;

import java.util.List;
import java.util.Map;

public class EndTurnDelta extends GameDelta {
    private final Map<String, Integer> updatedFood;        // nickname -> cibo aggiornato
    private final Map<String, Integer> updatedPrestige;    // nickname -> PP aggiornati
    private final List<String> newUpperTribeIds;             // nuove carte personaggio ed evento nella fila superiore
    private final List<String> newLowerTribeIds;             // carte spostate nella fila inferiore
    private final List<String> newUpperBuildingIds;     // nuovi edifici (se cambio era)
    private final List<String> newLowerBuildingIds;
    private final boolean eraChanged;
    private final boolean gameEnded;

    public EndTurnDelta(Map<String, Integer> updatedFood,
                        Map<String, Integer> updatedPrestige,
                        List<String> newUpperTribeIds,
                        List<String> newLowerTribeIds,
                        List<String> newUpperBuildingIds,
                        List<String> newLowerBuildingIds,
                        boolean eraChanged, boolean gameEnded) {
        this.updatedFood = updatedFood;
        this.updatedPrestige = updatedPrestige;
        this.newUpperTribeIds = newUpperTribeIds;
        this.newLowerTribeIds = newLowerTribeIds;
        this.newUpperBuildingIds = newUpperBuildingIds;
        this.newLowerBuildingIds = newLowerBuildingIds;
        this.eraChanged = eraChanged;
        this.gameEnded = gameEnded;
    }

    // getter per tutti i campi
    public Map<String, Integer> getUpdatedFood() { return updatedFood; }
    public Map<String, Integer> getUpdatedPrestige() { return updatedPrestige; }
    public List<String> getNewUpperRowIds() { return newUpperTribeIds; }
    public List<String> getNewLowerRowIds() { return newLowerTribeIds; }
    public List<String> getNewBuildingUpperRowIds() { return newUpperBuildingIds; }
    public List<String> getNewBuildingLowerRowIds() { return newLowerBuildingIds; }
    public boolean isEraChanged() { return eraChanged; }
    public boolean isGameEnded() { return gameEnded; }
}
