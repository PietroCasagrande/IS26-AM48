package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.control.Label;

/**
 * Controller for the splash screen scene.
 * Displays the game title and a pulsing "press any key" label.
 * Transitions to the main menu when the user clicks anywhere or presses a key.
 */
public class SplashScreenController {

    @FXML
    public StackPane root;
    @FXML
    public VBox textBox;
    @FXML
    public Label pressAnyButtonLabel;

    private FadeTransition pulse;

    private VirtualServer server;
    private ClientModel model;

    /**
     * Injects the server reference to pass to subsequent scenes.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference to pass to subsequent scenes.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
    }

    /**
     * Initializes the splash screen with a pulsing fade animation on the instruction label,
     * and sets up mouse click and key press listeners to navigate to the main menu.
     */
    @FXML
    public void initialize() {
        // Pulsing animation on the "press any button" label
        pulse = new FadeTransition(Duration.seconds(1.2), pressAnyButtonLabel);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Listen for mouse clicks to transition to menu
        root.setOnMouseClicked(_-> handleMenu());

        // Listen for keyboard presses to transition to menu
        root.setFocusTraversable(true);
        root.setOnKeyPressed(_ -> handleMenu());
    }

    /**
     * Transitions from the splash screen to the main menu scene.
     * Passes the server and model references to the menu controller.
     */
    private void handleMenu() {
        MesosMenuController menuController = (MesosMenuController) SceneManager.changeScene("mesos-menu.fxml");

        // Pass server and model references to the menu controller
        if (menuController != null) {
            menuController.setServer(server);
            menuController.setModel(model);
        }
    }
}
