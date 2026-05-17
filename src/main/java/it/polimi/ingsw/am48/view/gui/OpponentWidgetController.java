package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.view.CardDataRegistry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class OpponentWidgetController {
    @FXML private ImageView avatar;
    @FXML private Label labelNickname;
    private CardDataRegistry cardDataRegistry;
    private ClientModel model;
    private VirtualServer server;
    private ImageCache imageCache;

    @FXML
    public void initialize() {
        labelNickname.setOnMouseClicked(event -> openPlayerTribePopup(labelNickname.getText()));
    }

    public void setPlayerData(String nickname, Image avatarImage) {
        labelNickname.setText(nickname);
        avatar.setImage(avatarImage);
    }

    private void openPlayerTribePopup(String nickname) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/polimi/ingsw/am48/view/gui/player-tribe-screen.fxml"));
            Parent root = loader.load();

            PlayerTribeController tribeController = loader.getController();

            // prima devo inserire le dipendenze, altrimenti ci sarebbe un NullPointerException)
            tribeController.setModel(this.model);
            tribeController.setServer(this.server);
            tribeController.setDependencies(this.cardDataRegistry, this.imageCache);

            // ora si può inizializzare la pagina
            tribeController.initialize(nickname);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Tribù di " + nickname);
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}