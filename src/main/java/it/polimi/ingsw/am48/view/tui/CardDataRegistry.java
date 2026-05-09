package it.polimi.ingsw.am48.view.tui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/*
 * Loads card display data from client_data.json and provides compact label strings for each card ID.
 * Used by CliRenderer to enrich output.
 *
 * For cards absent from the JSON, the renderer prints only the card ID.
 */
public class CardDataRegistry {

    // Maps cardId -> compact display label, e.g. "food", "arrow", "★★", "cost:5 pp:3"
    private final Map<String, String> labels = new HashMap<>();

    private final Map<String, String> descriptions = new HashMap<>();
    /*
     * Loads and parses client_data.json from the classpath.
     * Called once at client startup.
     */
    public CardDataRegistry() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/client_data.json");
            if (inputStream == null) {
                System.err.println("[CardDataRegistry] client_data.json not found.");
                return;
            }
            JsonNode root = new ObjectMapper().readTree(inputStream);
            parseCharacters(root.get("characters"));
            parseEvents(root.get("events"));
            parseBuildings(root.get("buildings"));
        } catch (Exception e) {
            System.err.println("[CardDataRegistry] Failed to load card data: " + e.getMessage());
        }
    }


    // Returns a compact label for the given card ID, or empty string if unknown.
    public String getLabel(String cardId) {
        return labels.getOrDefault(cardId, "");
    }

    // Returns the full description for a building card, or empty string if not found
    public String getDescription(String cardId) {
        return descriptions.getOrDefault(cardId, "");
    }

    // -------------------------------------------------------------------------
    // Parsers — one per card family
    // -------------------------------------------------------------------------

    private void parseCharacters(JsonNode array) {
        if (array == null) return;
        for (JsonNode node : array) {
            String id = node.get("id").asText();
            JsonNode stats = node.get("stats");
            labels.put(id, buildCharacterLabel(stats));
        }
    }

    private void parseEvents(JsonNode array) {
        if (array == null) return;
        for (JsonNode node : array) {
            String id = node.get("id").asText();
            JsonNode stats = node.get("stats");
            labels.put(id, buildEventLabel(stats));
        }
    }

    private void parseBuildings(JsonNode array) {
        if (array == null) return;
        for (JsonNode node : array) {
            String id = node.get("id").asText();
            JsonNode stats = node.get("stats");
            labels.put(id, buildBuildingLabel(stats));

            // save full description for "info" command
            if(node.has("description")) {
                descriptions.put(id, node.get("description").asText());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Label builders: build a label for character cards based on which stats field is present.
    // -------------------------------------------------------------------------

    // Builds a label for character cards
    private String buildCharacterLabel(JsonNode stats) {
        if (stats == null) return "";

        // resource  -> "food"
        if (stats.has("resource"))
            return stats.get("resource").asText().toLowerCase();

        // artifact  -> artifact name lowercase (e.g. "arrow")
        if (stats.has("artifact"))
            return stats.get("artifact").asText().toLowerCase();

        // quantity  -> star symbols (e.g. "★★" for quantity=2)
        if (stats.has("quantity")) {
            int q = stats.get("quantity").asInt();
            return "★".repeat(q);
        }

        // buildingDiscount + builderPp -> "disc:1 pp:2"
        if (stats.has("buildingDiscount")) {
            int disc = stats.get("buildingDiscount").asInt();
            int pp   = stats.get("builderPp").asInt();
            return "disc:" + disc + " pp:" + pp;
        }

        return "";
    }

    // Builds a label for event cards.
    private String buildEventLabel(JsonNode stats) {
        if (stats == null) return "";

        // EVA: "thresh:N +W/-L"
        if (stats.has("threshold"))
            return "thresh:" + stats.get("threshold").asInt()
                    + " +" + stats.get("winRes").asInt()
                    + "/" + stats.get("loseRes").asInt();

        // EVH: "+N pp"
        if (stats.has("ppGained"))
            return "+" + stats.get("ppGained").asInt() + "pp";

        // EVS: "min:N max:M"
        if (stats.has("ppToMin"))
            return "min:" + stats.get("ppToMin").asInt()
                    + " max:+" + stats.get("ppToMax").asInt();

        // EVP: "-N pp"
        if (stats.has("ppLost"))
            return "-" + stats.get("ppLost").asInt() + "pp";

        return "";
    }


    // Builds a label for building cards: "cost:N pp:M"
    private String buildBuildingLabel(JsonNode stats) {
        if (stats == null) return "";
        int cost = stats.get("foodCost").asInt();
        int pp = stats.get("prestigePoints").asInt();
        return "cost:" + cost + " pp:" + pp;
    }
}