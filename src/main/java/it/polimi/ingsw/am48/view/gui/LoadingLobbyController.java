package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class LoadingLobbyController {
    @FXML
    private StackPane rootLobby;
    @FXML
    private HBox loadingBox;
    @FXML
    private Label waitingLabel;
    @FXML
    private ImageView totem;

    private VirtualServer server;
    private ClientModel model;

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

    private void startRotation() {
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), totem);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
    }

    @FXML
    public void initialize() {
        // 3. Animazione del testo (Puntini dinamici)
        /*Timeline dotAnimation = new Timeline(new KeyFrame(Duration.seconds(0.6), e -> {
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
