package it.polimi.ingsw.am48.view.gui;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches and provides access to all image assets used by the GUI.
 * Images are loaded on demand and stored in an internal map to avoid repeated disk I/O.
 * Provides specialized rendering methods for cards, totems, rules pages, the summary card,
 * and prestige point indicators.
 */
public class ImageCache {
    private final Map<String, String> paths;
    private final Map<String, Image> cache;
    private final int numRulesPages;

    /**
     * Constructs an ImageCache with the given path mappings.
     * @param imagePaths a map from asset identifiers to their resource paths
     */
    public ImageCache(Map<String, String> imagePaths) {
        this.paths = imagePaths;
        this.cache = new HashMap<>();
        this.numRulesPages = 8;
    }

    /**
     * Loads and returns the image for a card identified by its ID.
     * The result is cached for subsequent requests.
     * @param cardId the card identifier
     * @return the card Image, or null if the path is unknown or loading fails
     */
    public Image renderCards(String cardId) {
        String path = paths.get(cardId);

        if (path == null || path.isEmpty()) {
            return null;
        }

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                Image img = new Image(is);
                cache.put(path, img); // Cache the image for future use
                return img;
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
        }

        return null;
    }

    /**
     * Preloads all rules pages (1 through 8) using background loading to avoid UI freezes.
     * @return a list of Images for each rules page
     */
    public List<Image> preloadRules() {

        List<Image> rules = new ArrayList<>();

        for(int i = 1; i <= numRulesPages; i++) {
            String path = "/it/polimi/ingsw/am48/view/gui/images/rules/rules_" + i + ".png";

            try {
                // JavaFX trick: background loading requires a URL string, not an InputStream
                java.net.URL resource = getClass().getResource(path);

                if (resource != null) {
                    String absoluteUrl = resource.toExternalForm();

                    // The second parameter 'true' enables background loading in a separate thread,
                    // preventing the GUI from freezing during image loading.
                    Image ruleImg = new Image(absoluteUrl, true);
                    rules.add(ruleImg);
                } else {
                    System.err.println("Resource not found: " + path);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + path);
            }
        }
        return rules;
    }

    /**
     * Loads and returns the front and back images of the summary card.
     * Results are cached after the first load.
     * @return a list with two elements: [front, back]
     */
    public List<Image> renderSummaryCard(){

        String frontPath = "/it/polimi/ingsw/am48/view/gui/images/summaryCard/summary-card-front.png";
        String backPath = "/it/polimi/ingsw/am48/view/gui/images/summaryCard/summary-card-back.png";
        List<Image> summaryCard = new ArrayList<>();

        // Load both summary card images if not already cached
        if (!this.cache.containsKey(frontPath) || !this.cache.containsKey(backPath)) {
            try {
                Image frontImg = new Image(getClass().getResourceAsStream(frontPath));
                Image backImg = new Image(getClass().getResourceAsStream(backPath));

                this.cache.put(frontPath, frontImg);
                this.cache.put(backPath, backImg);

            } catch (Exception e) {
                System.err.println("Error: cannot upload summary card.");
            }
        }

        summaryCard.add(this.cache.get(frontPath));
        summaryCard.add(this.cache.get(backPath));
        return summaryCard;
    }

    /**
     * Loads and returns the prestige points icon.
     * A different image is returned when prestige is negative.
     * @param negative whether the player's prestige points are below zero
     * @return the prestige icon Image
     */
    public Image renderPrestige(boolean negative) {
        String path = negative
                ? "/it/polimi/ingsw/am48/view/gui/images/negPrestigePoints.png"
                : "/it/polimi/ingsw/am48/view/gui/images/prestigePoints.png";

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                Image img = new Image(is);
                cache.put(path, img);
                return img;
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento immagine prestige: " + path);
        }

        return null;
    }

    /**
     * Loads and returns the totem image for the given color.
     * The result is cached after the first load.
     * @param color the totem color name (e.g. "blue", "red")
     * @return the totem Image
     */
    public Image renderTotem(String color){

        String totemPath = "/it/polimi/ingsw/am48/view/gui/images/totems/"
                + color.toLowerCase()
                + "Totem.png";

        if(!this.cache.containsKey(totemPath)) {
            try {
                Image totemImage = new Image(getClass().getResourceAsStream(totemPath));
                this.cache.put(totemPath, totemImage);
            } catch (Exception e) {
                System.err.println("Error: cannot upload" + color + "totem.");
            }
        }
        return this.cache.get(totemPath);
    }
}
