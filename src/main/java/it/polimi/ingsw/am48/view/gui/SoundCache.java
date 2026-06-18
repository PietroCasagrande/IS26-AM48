package it.polimi.ingsw.am48.view.gui;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches and plays sound effects for the GUI.
 * Audio clips are loaded on first use and stored in an internal map
 * so that repeated playback does not require reloading from disk.
 */
public class SoundCache {
    private Map<String, AudioClip> cache = new HashMap<>();

    /**
     * Plays the sound effect for placing a totem.
     * The audio clip is loaded once and then cached.
     */
    public void playTotem() {
        String path = "/it/polimi/ingsw/am48/view/gui/audio/place-totem-sound.wav";

        if (!cache.containsKey(path)) {
            try {
                String url = getClass().getResource(path).toExternalForm();
                AudioClip clip = new AudioClip(url);
                clip.setVolume(0.5);
                cache.put(path, clip);
            } catch (Exception e) {
                System.err.println("Impossible loading the sound: " + path);
                return;
            }
        }

        // Instantly play sound
        cache.get(path).play();
    }

    /**
     * Plays the sound effect for taking a card.
     * The audio clip is loaded once and then cached.
     */
    public void playCard() {
        String path = "/it/polimi/ingsw/am48/view/gui/audio/take-card-sound.wav";

        if (!cache.containsKey(path)) {
            try {
                String url = getClass().getResource(path).toExternalForm();
                AudioClip clip = new AudioClip(url);
                clip.setVolume(0.8);
                cache.put(path, clip);
            } catch (Exception e) {
                System.err.println("Impossible loading the sound: " + path);
                return;
            }
        }

        // Instantly play sound
        cache.get(path).play();
    }

    /**
     * Plays the error sound effect (a distinctive "FAAAAH" sound).
     * This is triggered when the server returns an error to the client.
     */
    public void playFAAAAH() {
        String path = "/it/polimi/ingsw/am48/view/gui/audio/FAAAAAAAH.wav";

        if (!cache.containsKey(path)) {
            try {
                String url = getClass().getResource(path).toExternalForm();
                AudioClip clip = new AudioClip(url);
                clip.setVolume(0.5);
                cache.put(path, clip);
            } catch (Exception e) {
                System.err.println("Impossible loading the sound: " + path);
                return;
            }
        }

        // Instantly play sound
        cache.get(path).play();
    }
}