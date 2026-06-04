package it.polimi.ingsw.am48.view.gui;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageCache {
    private final Map<String, String> paths;
    private final Map<String, Image> cache;
    private final int numRulesPages;

    public ImageCache(Map<String, String> imagePaths) {
        this.paths = imagePaths;
        this.cache = new HashMap<>();
        this.numRulesPages = 8;
    }

    private Image loadAsync(String path) {
        if (path == null || path.isEmpty()) return null;

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                Image img = new Image(resource.toExternalForm(), true);
                cache.put(path, img);
                return img;
            } else {
                System.err.println("Risorsa non trovata: " + path);
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento asincrono: " + path);
        }
        return null;
    }

    public Image renderCards(String cardId) {
        String path = paths.get(cardId);
        return loadAsync(path);
    }

    public List<Image> preloadRules() {
        List<Image> rules = new ArrayList<>();
        for(int i = 1; i <= numRulesPages; i++) {
            String path = "/it/polimi/ingsw/am48/view/gui/images/rules/rules_" + i + ".png";
            Image img = loadAsync(path);
            if (img != null) rules.add(img);
        }
        return rules;
    }

    public List<Image> renderSummaryCard(){
        String frontPath = "/it/polimi/ingsw/am48/view/gui/images/summaryCard/summary-card-front.png";
        String backPath = "/it/polimi/ingsw/am48/view/gui/images/summaryCard/summary-card-back.png";

        Image frontImg = loadAsync(frontPath);
        Image backImg = loadAsync(backPath);

        List<Image> summaryCard = new ArrayList<>();
        if (frontImg != null) summaryCard.add(frontImg);
        if (backImg != null) summaryCard.add(backImg);
        return summaryCard;
    }

    public Image renderPrestige(boolean negative) {
        String path = negative
                ? "/it/polimi/ingsw/am48/view/gui/images/negPrestigePoints.png"
                : "/it/polimi/ingsw/am48/view/gui/images/prestigePoints.png";
        return loadAsync(path);
    }

    public Image renderTotem(String color){
        String totemPath = "/it/polimi/ingsw/am48/view/gui/images/totems/"
                + color.toLowerCase()
                + "Totem.png";
        return loadAsync(totemPath);
    }
}