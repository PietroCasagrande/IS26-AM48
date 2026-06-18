package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
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

/**
 * Controller for the opponent widget displayed on the sides of the game board.
 * Shows an opponent's nickname, totem avatar, food count, and prestige points.
 * The nickname is clickable to open a popup showing the opponent's full tribe.
 */
public class OpponentWidgetController {
    @FXML private ImageView avatar;
    @FXML private Label labelNickname;
    @FXML private Label foodCount;
    @FXML private Label prestigeCount;
    @FXML private ImageView prestigeImage;
    private ClientModel model;
    private VirtualServer server;
    private ImageCache imageCache;

    /**
     * Injects the client model reference.
     * @param model the client model
     */
    public void setModel(ClientModel model)       { this.model = model; }

    /**
     * Injects the server reference.
     * @param server the virtual server
     */
    public void setServer(VirtualServer server)   { this.server = server; }

    /**
     * Injects the image cache dependency.
     * @param cache the shared {@link ImageCache}
     */
    public void setDependencies(ImageCache cache) { this.imageCache = cache; }

    /**
     * Initializes the widget by setting a click handler on the nickname label
     * that opens the opponent's tribe popup.
     */
    @FXML
    public void initialize() {
        labelNickname.setOnMouseClicked(event -> openPlayerTribePopup(labelNickname.getText()));
    }

    /**
     * Sets the player's nickname and totem avatar image.
     * @param nickname the player's display name
     * @param avatarImage the player's totem image
     */
    public void setPlayerData(String nickname, Image avatarImage) {
        labelNickname.setText(nickname);
        avatar.setImage(avatarImage);
    }

    /**
     * Updates the displayed food and prestige values for this opponent.
     * The prestige icon is switched to the negative variant when points are below zero.
     * @param food the opponent's current food count
     * @param prestige the opponent's current prestige points
     */
    public void setPlayerStats(int food, int prestige) {
        foodCount.setText(String.valueOf(food));
        prestigeCount.setText(String.valueOf(prestige));
        if (imageCache != null) {
            prestigeImage.setImage(imageCache.renderPrestige(prestige < 0));
        }
    }

    /**
     * Opens a modal popup window displaying the full tribe of the given opponent.
     * Loads the player-tribe-screen FXML, initializes the controller with the opponent's data,
     * and shows it as a blocking modal dialog.
     * @param nickname the opponent's nickname whose tribe to display
     */
    private void openPlayerTribePopup(String nickname) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/it/polimi/ingsw/am48/view/gui/player-tribe-screen.fxml"));
            Parent root = loader.load();

            PlayerTribeController tribeController = loader.getController();

            // Dependencies must be injected before initialization to avoid NullPointerException
            tribeController.setModel(this.model);
            tribeController.setServer(this.server);
            tribeController.setDependencies(this.imageCache);

            // Now the page can be initialized
            tribeController.initialize(nickname);

            // Update the opponent's tribe display
            tribeController.updateTribe();

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