package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import javafx.scene.control.ScrollPane;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MesosMenuController {

    @FXML
    private StackPane root;
    @FXML
    private VBox titleBox;
    @FXML
    private VBox buttonBox;
    @FXML
    private Button buttonPlay;
    @FXML
    private Button buttonHowToPlay;
    @FXML
    private Button buttonSettings;
    @FXML
    private Button buttonExit;
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

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        // Preloading rules
        List<Image> rulesList = SceneManager.getImageCache().preloadRules();
        for(Image i : rulesList) {
            ImageView ruleImgV = new ImageView(i);
            ruleImgV.setFitWidth(1000.0);
            ruleImgV.setPreserveRatio(true);
            this.rules.add(ruleImgV);
        }
    }

    @FXML
    public void handlePlay() {
        JoinGameController joinGameScene = (JoinGameController) SceneManager.changeScene("join-game-screen.fxml");

        // Passiamo i riferimenti al nuovo controller
        if (joinGameScene != null) {
            joinGameScene.setServer(server);
            joinGameScene.setModel(model);
        }
    }

    // Metodo collegato al tasto "How to Play"
    @FXML
    private void handleHowToPlay() {
        // Changing visibility
        menuContainer.setVisible(false);
        rulesOverlay.setVisible(true);

        // Rendering rules images just for the very first time
        if (!rulesLoaded) {
            for(ImageView iv : rules) {
                rulesImagesContainer.getChildren().add(iv);
            }
            rulesLoaded = true;
        }
    }

    // Metodo collegato al tasto "Indietro" dentro il manuale
    @FXML
    private void handleCloseRules() {
        // Nasconde il manuale e torna al menu
        rulesOverlay.setVisible(false);
        menuContainer.setVisible(true);
    }

    @FXML
    private void handleExit() {
        // Chiude l'applicazione JavaFX
        Platform.exit();

        // Termina il processo Java
        System.exit(0);
    }
}
