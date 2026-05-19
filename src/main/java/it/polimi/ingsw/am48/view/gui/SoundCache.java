package it.polimi.ingsw.am48.view.gui;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public class SoundCache {
    private Map<String, AudioClip> cache = new HashMap<>();

    public void playTotem() {
        String path = "/it/polimi/ingsw/am48/view/gui/audio/place-totem-sound.wav";

        if (!cache.containsKey(path)) {
            try {
                String url = getClass().getResource(path).toExternalForm();
                AudioClip clip = new AudioClip(url);
                clip.setVolume(0.8);
                cache.put(path, clip);
            } catch (Exception e) {
                System.err.println("Impossibile caricare il suono: " + path);
                return;
            }
        }

        // Instantly play sound
        cache.get(path).play();
    }

    public void playCard() {
        String path = "/it/polimi/ingsw/am48/view/gui/audio/take-card-sound.wav";

        if (!cache.containsKey(path)) {
            try {
                String url = getClass().getResource(path).toExternalForm();
                AudioClip clip = new AudioClip(url);
                clip.setVolume(0.8);
                cache.put(path, clip);
            } catch (Exception e) {
                System.err.println("Impossibile caricare il suono: " + path);
                return;
            }
        }

        // Instantly play sound
        cache.get(path).play();
    }
}