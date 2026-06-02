package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Controller for the loading lobby scene shown while waiting for other players to join.
 * Displays a rotating totem animation and a "Waiting for players" label.
 */
public class LoadingLobbyController {
    @FXML
    private ImageView totem;

    private VirtualServer server;
    private ClientModel model;

    /**
     * Injects the server reference for future scene transitions.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference for future scene transitions.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
    }

    /**
     * Starts an animation on the totem image.
     */
    private void startRotation() {
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), totem);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
    }

    /**
     * Initializes the loading lobby by starting the totem rotation animation.
     */
    @FXML
    public void initialize() {
        // Commented out: animated dots for the waiting label (can be re-enabled if desired)
        /*
        Timeline dotAnimation = new Timeline(new KeyFrame(Duration.seconds(0.6), e -> {
            String current = waitingLabel.getText();
            if (current.equals("Waiting for players . . .")) {
                waitingLabel.setText("Waiting for players");
            } else {
                waitingLabel.setText(current + " .");
            }
        }));
        dotAnimation.setCycleCount(Animation.INDEFINITE);
        dotAnimation.play(); */
        startRotation();
    }
}
