package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the player tribe view, showing all character and building cards
 * owned by a specific player. The cards are organized into columns by type
 * (Artists, Builders, Hunters, Inventors, Pickers, Shamans, Buildings).
 * Can be displayed either as an embedded widget or as a full modal popup.
 */
public class PlayerTribeController {
    @FXML private BorderPane rootTribe;
    @FXML private BorderPane titlePane;
    @FXML private Label titleLabel;
    @FXML private VBox artistsColumn;
    @FXML private VBox buildersColumn;
    @FXML private VBox huntersColumn;
    @FXML private VBox inventorsColumn;
    @FXML private VBox pickersColumn;
    @FXML private VBox shamansColumn;
    @FXML private VBox buildingsColumn;

    private VirtualServer server;
    private ClientModel model;
    private ImageCache imageCache;
    private String myNickname;

    /**
     * Injects the client model reference.
     * @param model the client model
     */
    public void setModel(ClientModel model) {
        this.model = model;
    }

    /**
     * Injects the server reference.
     * @param server the virtual server
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the image cache dependency.
     * @param cache the shared {@link ImageCache}
     */
    public void setDependencies(ImageCache cache) {
        this.imageCache = cache;
    }

    /**
     * Initializes the tribe view for a given player nickname.
     * Delegates to the full {@link #initialize(VirtualServer, ClientModel, String)} method.
     * @param nickname the player whose tribe to display
     */
    public void initialize(String nickname) {
        initialize(this.server, this.model, nickname);
    }

    /**
     * Initializes the tribe controller with server, model, and player identity.
     * Sets the title label to show whose tribe is being displayed.
     * @param server the virtual server reference
     * @param model the client model
     * @param nickname the player whose tribe to display
     */
    @FXML
    public void initialize(VirtualServer server, ClientModel model, String nickname) {
        this.server = server;
        this.model = model;
        this.myNickname = nickname;
        this.titleLabel.setText(myNickname + "'s Tribe");
    }

    /**
     * Refreshes the tribe display to reflect the current game state.
     * Clears all card columns and repopulates them with the player's character and building cards.
     * Cards are assigned to the appropriate column based on their ID prefix.
     */
    public void updateTribe(){
        Platform.runLater(() -> {
            clearAllColumns();

            List<String> characters = model.getState().getPlayer(this.myNickname).getCharacterCardIds();
            List<String> buildings = model.getState().getPlayer(this.myNickname).getBuildingCardIds();

            for (String cardId : characters) {
                ImageView cardView = createImageView(cardId);

                if (cardView != null) {

                    cardView.setPreserveRatio(true);
                    cardView.fitWidthProperty().bind(artistsColumn.widthProperty().multiply(0.85));

                    if (cardId.startsWith("ART")) {
                        artistsColumn.getChildren().add(cardView);
                    } else if (cardId.startsWith("BUI")) {
                        buildersColumn.getChildren().add(cardView);
                    } else if (cardId.startsWith("HUN")) {
                        huntersColumn.getChildren().add(cardView);
                    } else if (cardId.startsWith("INV")) {
                        inventorsColumn.getChildren().add(cardView);
                    } else if (cardId.startsWith("PIC")) {
                        pickersColumn.getChildren().add(cardView);
                    } else if (cardId.startsWith("SHA")) {
                        shamansColumn.getChildren().add(cardView);
                    } else {
                        System.err.println("Unknown character card prefix: " + cardId);
                    }
                }
            }

            for (String cardId : buildings) {
                ImageView cardView = createImageView(cardId);

                if (cardView != null) {

                    cardView.setPreserveRatio(true);
                    cardView.fitWidthProperty().bind(buildingsColumn.widthProperty().multiply(0.85));

                    buildingsColumn.getChildren().add(cardView);
                }
            }
        });
    }

    /**
     * Helper method to convert a card ID into a graphical ImageView node.
     * Applies standard sizing and a drop shadow effect.
     * @param cardId the card identifier
     * @return an ImageView for the card, or null if the image could not be loaded
     */
    private ImageView createImageView(String cardId) {
        Image img = imageCache.renderCards(cardId);

        if (img == null) {return null;}

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(150.0);
        imageView.setPreserveRatio(true);
        imageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.5)));

        return imageView;
    }

    /**
     * Clears all card column containers before repopulating them.
     */
    private void clearAllColumns() {
        if (artistsColumn != null) artistsColumn.getChildren().clear();
        if (buildersColumn != null) buildersColumn.getChildren().clear();
        if (huntersColumn != null) huntersColumn.getChildren().clear();
        if (inventorsColumn != null) inventorsColumn.getChildren().clear();
        if (pickersColumn != null) pickersColumn.getChildren().clear();
        if (shamansColumn != null) shamansColumn.getChildren().clear();
        if (buildingsColumn != null) buildingsColumn.getChildren().clear();
    }

    /**
     * Configures this tribe view for embedded display within the game board,
     * hiding the title bar and collapsing its space so it does not leave an empty gap.
     * Adds a "mini-mode" CSS class for styling adjustments.
     */
    public void setEmbeddedMode() {

        // 1. Hide the title bar
        titlePane.setVisible(false);

        // 2. Collapse the title bar's layout space (otherwise an empty gap remains)
        titlePane.setManaged(false);

        // 3. Add a special CSS class for mini-mode styling
        rootTribe.getStyleClass().add("mini-mode");
    }

    /**
     * Handles the exit/close button click. Closes the popup stage.
     */
    @FXML
    public void handleExit() {
        Stage stage = (Stage) rootTribe.getScene().getWindow();
        stage.close();
    }
}
