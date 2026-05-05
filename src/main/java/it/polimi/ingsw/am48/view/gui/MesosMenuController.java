package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

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

    private VirtualServerRmi server;
    private ClientModel model;

    public void setServer(VirtualServerRmi server) {
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
        LoadingLobbyController loadingLobby = (LoadingLobbyController) SceneManager.changeScene("loading-lobby.fxml");

        // Passiamo i riferimenti al nuovo controller
        if (loadingLobby != null) {
            loadingLobby.setServer(server);
            loadingLobby.setModel(model);
        }
    }
}
