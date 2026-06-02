package it.polimi.ingsw.am48.view.gui;

import javafx.scene.image.Image;
import java.net.URL;
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
     * Loads an image from the given resource path using JavaFX background loading.
     * Already-cached images are returned immediately without additional I/O.
     *
     * @param path the resource path to the image file
     * @return the loaded Image, or {@code null} if the path is invalid or the resource is not found
     */
    private Image loadAsync(String path) {
        if (path == null || path.isEmpty()) return null;

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                // 'true' enables JavaFX's native background loading
                Image img = new Image(resource.toExternalForm(), true);
                cache.put(path, img);
                return img;
            } else {
                System.err.println("Resource not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error during asynchronous loading: " + path);
        }

        return null;
    }

    /**
     * Loads and returns the image for the card identified by the given ID.
     * The card's resource path is resolved through the internal path map and cached after the first load.
     *
     * @param cardId the identifier of the card to render
     * @return the card Image, or {@code null} if the card ID is unknown or the resource is missing
     */
    public Image renderCards(String cardId) {
        String path = paths.get(cardId);
        return loadAsync(path);
    }

    /**
     * Preloads all rules explanation pages (1 through 8) using background loading to avoid UI freezes.
     *
     * @return a list of Images for each rules page that was successfully loaded
     */
    public List<Image> preloadRules() {
        List<Image> rules = new ArrayList<>();

        for(int i = 1; i <= numRulesPages; i++) {
            String path = "/it/polimi/ingsw/am48/view/gui/images/rules/rules_" + i + ".png";
            Image img = loadAsync(path);
            if (img != null) rules.add(img);
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
        // Trigger asynchronous loading for both sides; cached on first load
        Image frontImg = loadAsync(frontPath);
        Image backImg = loadAsync(backPath);
        List<Image> summaryCard = new ArrayList<>();
        if (frontImg != null) summaryCard.add(frontImg);
        if (backImg != null) summaryCard.add(backImg);
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
        return loadAsync(path);
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
        return loadAsync(totemPath);
    }
}
