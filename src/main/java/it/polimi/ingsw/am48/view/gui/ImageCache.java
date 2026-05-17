package it.polimi.ingsw.am48.view.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
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

    //Render card images
    public Image renderImage(String cardId) {
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
                cache.put(path, img); // Salvataggio in cache per usi futuri
                return img;
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento immagine: " + path);
        }

        return null;
    }

    //Render rules images
    public List<Image> preloadRules() {

        List<Image> rules = new ArrayList<>();

        for(int i = 1; i <= numRulesPages; i++) {
            String path = "/it/polimi/ingsw/am48/view/gui/images/rules/rules_" + i + ".png";

            try {
                // TRUCCO JAVAFX: Per il caricamento in background SERVE l'URL testuale, non l'InputStream!
                java.net.URL resource = getClass().getResource(path);

                if (resource != null) {
                    String absoluteUrl = resource.toExternalForm();

                    // IL SEGRETO È QUI: Il secondo parametro 'true' ordina a JavaFX
                    // di caricare l'immagine in un thread separato (Background Loading).
                    // La GUI non si bloccherà per un singolo millisecondo!
                    Image ruleImg = new Image(absoluteUrl, true);
                    rules.add(ruleImg);
                } else {
                    System.err.println("Risorsa non trovata: " + path);
                }
            } catch (Exception e) {
                System.err.println("Errore pre-caricamento immagine: " + path);
            }
        }
        return rules;
    }
}
