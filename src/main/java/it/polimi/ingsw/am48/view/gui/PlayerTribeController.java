package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ImageCache imageCache;
    private String myNickname;

    public void setDependencies(ImageCache cache) {
        this.imageCache = cache;
    }

    @FXML
    public void initialize(VirtualServer server, ClientModel model, String nickname) {
        this.server = server;
        this.model = model;
        this.myNickname = nickname;
        this.titleLabel.setText(myNickname + "'s Tribe");
    }

    public void updateTribe(ClientGameState state){
        Platform.runLater(() -> {
            clearAllColumns();

            List<String> characters = model.getState().getPlayer(this.myNickname).getCharacterCardIds();
            List<String> buildings = model.getState().getPlayer(this.myNickname).getBuildingCardIds();

            for (String cardId : characters) {
                ImageView cardView = createImageView(cardId);
                cardView.setPreserveRatio(true);
                cardView.fitWidthProperty().bind(artistsColumn.widthProperty().multiply(0.85));

                if (cardView != null) {
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
                        System.err.println("Prefisso carta personaggio sconosciuto: " + cardId);
                    }
                }
            }

            for (String cardId : buildings) {
                ImageView cardView = createImageView(cardId);
                cardView.setPreserveRatio(true);
                cardView.fitWidthProperty().bind(buildingsColumn.widthProperty().multiply(0.85));

                if (cardView != null) {
                    buildingsColumn.getChildren().add(cardView);
                }
            }
        });
    }

    /**
     * Metodo di supporto per trasformare un ID di una carta in un nodo ImageView grafico.
     * Applica le dimensioni standard ed eventuali effetti grafici.
     */
    private ImageView createImageView(String cardId) {
        Image img = imageCache.renderImage(cardId);

        if (img == null) {return null;}

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(150.0);
        imageView.setPreserveRatio(true);
        imageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.5)));

        return imageView;
    }

    /**
     * Svuota tutti i contenitori grafici prima di popolarli.
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

    public void setEmbeddedMode() {

        // 1. Rendiamo invisibile la barra
        titlePane.setVisible(false);

        // 2. Diciamo a JavaFX di far "collassare" lo spazio.
        // Senza questo, avresti un buco vuoto in alto!
        titlePane.setManaged(false);

        // 3. Aggiungiamo una classe CSS speciale a tutta la schermata (vedi step 2)
        rootTribe.getStyleClass().add("mini-mode");
    }

    @FXML
    public void handleExit() {
        //TODO
    }
}
