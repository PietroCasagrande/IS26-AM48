package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the main menu scene.
 * Provides buttons to play (navigate to join game), view rules (how to play), and exit the application.
 * Preloads and displays rule page images inside an overlay scroll pane.
 */
public class MesosMenuController {

    @FXML
    private VBox menuContainer;
    @FXML
    private ScrollPane rulesOverlay;
    @FXML
    private VBox rulesImagesContainer;

    private boolean rulesLoaded = false;
    private List<ImageView> rules = new ArrayList<>();

    private VirtualServer server;
    private ClientModel model;

    /**
     * Injects the server reference for scene transitions.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference for scene transitions.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
    }

    /**
     * Initializes the menu by preloading the rules images from the image cache.
     */
    @FXML
    public void initialize() {
        // Preload rule page images for the "How to Play" overlay
        List<Image> rulesList = SceneManager.getImageCache().preloadRules();
        for(Image i : rulesList) {
            ImageView ruleImgV = new ImageView(i);
            ruleImgV.setFitWidth(1000.0);
            ruleImgV.setPreserveRatio(true);
            this.rules.add(ruleImgV);
        }
    }

    /**
     * Handles the "Play" button click. Transitions to the join game screen.
     */
    @FXML
    public void handlePlay() {
        JoinGameController joinGameScene = (JoinGameController) SceneManager.changeScene("join-game-screen.fxml");

        // Pass server and model references to the new controller
        if (joinGameScene != null) {
            joinGameScene.setServer(server);
            joinGameScene.setModel(model);
        }
    }

    /**
     * Handles the "How to Play" button click.
     * Hides the main menu and shows the rules overlay with preloaded rule page images.
     */
    @FXML
    private void handleHowToPlay() {
        // Hide the menu and show the rules overlay
        menuContainer.setVisible(false);
        rulesOverlay.setVisible(true);

        // Render rule page images on first access
        if (!rulesLoaded) {
            for(ImageView iv : rules) {
                rulesImagesContainer.getChildren().add(iv);
            }
            rulesLoaded = true;
        }
    }

    /**
     * Handles the "Back" button inside the rules overlay.
     * Hides the rules overlay and returns to the main menu.
     */
    @FXML
    private void handleCloseRules() {
        // Hide the rules overlay and show the menu again
        rulesOverlay.setVisible(false);
        menuContainer.setVisible(true);
    }

    /**
     * Handles the "Exit" button click.
     * Exits the JavaFX application and terminates the Java process.
     */
    @FXML
    private void handleExit() {
        // Close the JavaFX application
        Platform.exit();

        // Terminate the Java process
        System.exit(0);
    }
}
