package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class PlayerTribeController {
    @FXML private BorderPane rootTribe;
    @FXML private VBox playerTribeBox;

    @FXML private BorderPane titlePane;
    @FXML private Label titleLabel;
    @FXML private Button exitButton;

    @FXML private GridPane charactersPane;
    @FXML private Label artistsLabel;
    @FXML private Label buildersLabel;
    @FXML private Label huntersLabel;
    @FXML private Label inventorsLabel;
    @FXML private Label pickersLabel;
    @FXML private Label shamansLabel;
    @FXML private Label buildingsLabel;

    @FXML private ScrollPane tribeScroll;
    @FXML private GridPane tribeGrid;
    @FXML private VBox artistsColumn;
    @FXML private VBox buildersColumn;
    @FXML private VBox huntersColumn;
    @FXML private VBox inventorsColumn;
    @FXML private VBox pickersColumn;
    @FXML private VBox shamansColumn;
    @FXML private VBox buildingsColumn;

    private VirtualServer server;
    private ClientModel model;

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

    @FXML
    public void initialize(String nickname) {
        List<String> characters = model.getState().getPlayer(nickname).getCharacterCardIds();
        List<String> buildings = model.getState().getPlayer(nickname).getBuildingCardIds();

        for (String characterId : characters) {
            //TODO
        }
    }


    @FXML
    public void handleExit() {

    }
}
