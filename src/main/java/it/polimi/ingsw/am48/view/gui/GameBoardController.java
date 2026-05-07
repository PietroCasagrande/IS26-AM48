package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class GameBoardController implements ModelObserver {

    @FXML private StackPane rootboard;

    // Bottom area
    @FXML private ScrollPane myHandBox;
    @FXML private Label prestigeCount;
    @FXML private Label foodCount;
    @FXML private Button bottone_avanti;

    // Player areas
    @FXML private VBox left_player, right_player, topLeft_player, topRight_player;
    @FXML private Label labelLeft, labelRight, labelTopLeft, labelTopRight;
    @FXML private ImageView avatarLeft, avatarRight, avatarTopLeft, avatarTopRight;

    // Board center
    @FXML private HBox upper_row, lower_row;
    @FXML private HBox deckable_sup, building_sup, deckable_inf, building_inf;
    @FXML private VBox offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;

    private VBox[] offerCards;
    private VBox[] opponentBoxes;
    private Label[] opponentLabels;

    @FXML
    public void initialize() {
        myNickname = SceneManager.getNickname();

        offerCards = new VBox[]{ offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG };
        opponentBoxes = new VBox[]{ left_player, right_player, topLeft_player, topRight_player };
        opponentLabels = new Label[]{ labelLeft, labelRight, labelTopLeft, labelTopRight };

        for (VBox b : opponentBoxes) b.setVisible(false);

        setupOfferCardClickHandlers();
    }

    public void setServer(VirtualServer server) { this.server = server; }

    public void setModel(ClientModel model) {
        this.model = model;
        model.registerObserver(this);
        if (model.getState() != null) onStateUpdated(model.getState());
    }

    @Override
    public void onStateUpdated(ClientGameState state) {
        Platform.runLater(() -> {
            if (myNickname == null) myNickname = SceneManager.getNickname();
            updatePlayerInfo(state);
            updateBoardRows(state);
            updateOfferCards(state);
            updateMyHand(state);
            updateTokens(state);
        });
    }

    // ─────────────────────── Player Info & Tooltip ───────────────────────

    private void updatePlayerInfo(ClientGameState state) {
        Map<String, ClientPlayerState> players = state.getPlayers();
        for (VBox b : opponentBoxes) b.setVisible(false);

        int slot = 0;
        for (ClientPlayerState player : players.values()) {
            if (player.getNickname().equals(myNickname)) continue;
            if (slot >= opponentBoxes.length) break;

            Label label = opponentLabels[slot];
            VBox box = opponentBoxes[slot];

            label.setText(player.getNickname());
            box.setVisible(true);

            // Uso un Tooltip avanzato invece di aggiungere nodi al VBox:
            // Questo evita che la UI "salti" quando passi il mouse sul nome.
            setupPlayerTooltip(label, player);
            slot++;
        }
    }

    private void setupPlayerTooltip(Label label, ClientPlayerState player) {
        Tooltip tooltip = new Tooltip();
        HBox container = new HBox(5);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #ffd700; -fx-border-width: 2;");

        // Aggiunge le immagini delle carte al tooltip
        addCardsToContainer(container, player.getCharacterCardIds(), 80);
        addCardsToContainer(container, player.getBuildingCardIds(), 80);

        tooltip.setGraphic(container);
        tooltip.setShowDelay(Duration.millis(100));
        label.setTooltip(tooltip);
    }

    private void addCardsToContainer(HBox container, List<String> ids, double height) {
        for (String id : ids) {
            ImageView img = createCardImageView(id, false);
            img.setFitHeight(height);
            container.getChildren().add(img);
        }
    }

    // ─────────────────────── Board & Rows ───────────────────────

    private void updateBoardRows(ClientGameState state) {
        updateRow(deckable_sup, state.getUpperRowCardIds());
        updateRow(building_sup, state.getBuildingUpperIds());
        updateRow(deckable_inf, state.getLowerRowCardIds());
        updateRow(building_inf, state.getBuildingLowerIds());
    }

    private void updateRow(HBox rowBox, List<String> cardIds) {
        rowBox.getChildren().clear();
        for (String id : cardIds) {
            rowBox.getChildren().add(createCardImageView(id, true));
        }
    }

    private void updateOfferCards(ClientGameState state) {
        Map<Character, String> positions = state.getOfferTrackPositions();
        for (int i = 0; i < 7; i++) {
            char letter = (char) ('A' + i);
            VBox offerBox = offerCards[i];
            offerBox.getChildren().clear();

            if (positions.containsKey(letter)) {
                Label lbl = new Label(positions.get(letter));
                lbl.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
                offerBox.getChildren().add(lbl);
            } else {
                offerBox.getChildren().add(new Label(String.valueOf(letter)));
            }
        }
    }

    // ─────────────────────── Utilities ───────────────────────

    private void updateMyHand(ClientGameState state) {
        ClientPlayerState myState = state.getPlayer(myNickname);
        if (myState == null) return;

        HBox handContainer = new HBox(10);
        handContainer.setPadding(new Insets(10));
        addCardsToContainer(handContainer, myState.getCharacterCardIds(), 150);
        addCardsToContainer(handContainer, myState.getBuildingCardIds(), 150);

        myHandBox.setContent(handContainer);
    }

    private void updateTokens(ClientGameState state) {
        ClientPlayerState myState = state.getPlayer(myNickname);
        if (myState != null) {
            foodCount.setText(String.valueOf(myState.getFood()));
            prestigeCount.setText(String.valueOf(myState.getPoints()));
        }
    }

    private ImageView createCardImageView(String cardId, boolean clickable) {
        ImageView img = new ImageView();
        // Nota: Assicurati che il percorso inizi con / e sia corretto rispetto ai resources
        String path = "/it/polimi/ingsw/am48/view/gui/images/cards/" + cardId + ".png";

        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            img.setImage(image);
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'immagine: " + path);
        }

        img.setPreserveRatio(true);
        img.setFitHeight(120);

        if (clickable) {
            img.getStyleClass().add("card-hover"); // Aggiungi questo nel tuo CSS per l'effetto ingrandimento
            img.setOnMouseClicked(e -> handleCardClick(cardId));
        }
        return img;
    }

    private void setupOfferCardClickHandlers() {
        for (int i = 0; i < offerCards.length; i++) {
            final char letter = (char) ('A' + i);
            offerCards[i].setOnMouseClicked(e -> handleOfferCardClick(letter));
        }
    }

    private void handleCardClick(String cardId) {
        if (server != null) {
            try { server.takeCard(myNickname, cardId); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void handleOfferCardClick(char letter) {
        if (server != null) {
            try { server.placeTotem(myNickname, letter); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @Override public void onError(String message) { System.err.println("Error: " + message); }
}