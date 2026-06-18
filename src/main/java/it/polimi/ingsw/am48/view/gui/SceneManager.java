package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.view.CardDataRegistry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Singleton-like manager for JavaFX scene transitions.
 * Holds global references to the primary Stage, the server connection, the client model,
 * and all shared resources (image cache, sound cache, card data registry, player nickname).
 * Provides the {@link #changeScene(String)} method to switch between FXML-based scenes.
 */
public class SceneManager {
    private static Stage stage;
    private static CardDataRegistry cardData;
    private static ImageCache imageCache;
    private static SoundCache soundCache;
    private static String nickname;

    /**
     * Initializes the SceneManager with the primary stage and shared resources.
     * Creates the card data registry, image cache, and sound cache.
     * @param primaryStage the JavaFX primary stage
     * @param s the virtual server reference
     * @param m the client model reference
     */
    public static void setup(Stage primaryStage, VirtualServer s, ClientModel m) {
        stage = primaryStage;
        cardData = new CardDataRegistry();
        imageCache = new ImageCache(cardData.getImagePaths());
        soundCache = new SoundCache();
    }

    /**
     * Stores the local player's nickname for use across scenes.
     * @param nick the player's nickname
     */
    public static void setNickname(String nick) {
        nickname = nick;
    }

    /**
     * Returns the local player's nickname.
     * @return the nickname string
     */
    public static String getNickname() {
        return nickname;
    }

    /**
     * Returns the shared image cache instance.
     * @return the {@link ImageCache}
     */
    public static ImageCache getImageCache() {return imageCache;}

    /**
     * Returns the shared sound cache instance.
     * @return the {@link SoundCache}
     */
    public static SoundCache getSoundCache() {return soundCache;}

    /**
     * Loads an FXML file and switches the current scene root to the new layout.
     * The scene dimensions (1280x720) are set on the first call.
     * @param fxmlPath the path to the FXML resource file (e.g. "splash-screen.fxml")
     * @return the controller of the newly loaded scene, or null if loading failed
     */
    public static Object changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Replace the scene content; set initial dimensions on first load
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root, 1280, 720));
            } else {
                stage.getScene().setRoot(root);
            }

            stage.show();

            // Return the new scene's controller for further setup
            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
