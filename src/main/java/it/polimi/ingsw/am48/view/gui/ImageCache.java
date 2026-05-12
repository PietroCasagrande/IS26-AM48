package it.polimi.ingsw.am48.view.gui;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private final Map<String, String> paths;
    private final Map<String, Image> cache;

    public ImageCache(Map<String, String> imagePaths) {
        this.paths = imagePaths;
        this.cache = new HashMap<>();
    }

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
}
