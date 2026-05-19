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
    @FXML private Label foodCount;
    @FXML private Label prestigeCount;
    private CardDataRegistry cardDataRegistry;
    private ClientModel model;
    private VirtualServer server;
    private ImageCache imageCache;

    public void setModel(ClientModel model)       { this.model = model; }
    public void setServer(VirtualServer server)   { this.server = server; }
    public void setDependencies(ImageCache cache) { this.imageCache = cache; }

    @FXML
    public void initialize() {
        labelNickname.setOnMouseClicked(event -> openPlayerTribePopup(labelNickname.getText()));
    }

    public void setPlayerData(String nickname, Image avatarImage) {
        labelNickname.setText(nickname);
        avatar.setImage(avatarImage);
    }

    public void setPlayerStats(int food, int prestige) {
        foodCount.setText(String.valueOf(food));
        prestigeCount.setText(String.valueOf(prestige));
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

            // aggiorna player tribe
            tribeController.updateTribe(this.model.getState());

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(nickname + "'s Tribe");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(true);
            popupStage.setMinWidth(600);
            popupStage.setMinHeight(400);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}