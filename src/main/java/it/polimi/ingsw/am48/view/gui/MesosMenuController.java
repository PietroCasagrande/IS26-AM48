package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.fxml.FXML;
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

    @FXML
    private VBox menuContainer; // Il contenitore del menu principale

    @FXML
    private ScrollPane rulesOverlay; // Lo ScrollPane del manuale

    // Metodo collegato al tasto "How to Play"
    @FXML
    private void handleHowToPlay() {
        // Nasconde il menu e mostra il manuale scorribile
        menuContainer.setVisible(false);
        rulesOverlay.setVisible(true);
    }

    // Metodo collegato al tasto "Indietro" dentro il manuale
    @FXML
    private void handleCloseRules() {
        // Nasconde il manuale e torna al menu
        rulesOverlay.setVisible(false);
        menuContainer.setVisible(true);
    }
}
