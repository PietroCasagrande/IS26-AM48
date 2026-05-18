package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.view.CardDataRegistry;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameBoardController implements ModelObserver {

    //Root
    @FXML private StackPane rootboard;

    // Bottom area
    @FXML private ScrollPane myHandBox;
    @FXML private PlayerTribeController playerTribeController;
    @FXML private Label prestigeCount;
    @FXML private Label foodCount;
    @FXML private Button bottone_avanti;
    @FXML private StackPane summaryCardContainer;
    @FXML private ImageView summaryCardImage;
    @FXML private Label flipIcon;

    // Player areas
    @FXML private VBox left_player1, right_player1, left_player2, right_player2;
    @FXML private Label labelLeft1, labelRight1, labelLeft2, labelRight2;
    @FXML private ImageView avatarLeft1, avatarRight1, avatarLeft2, avatarRight2;

    // Board center
    @FXML private GridPane boardCenter;
    @FXML private VBox center;
    @FXML private BoardCenterController boardCenterController;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;
    private CardDataRegistry cardData;
    private ImageCache imageCache;

    private VBox[] opponentBoxes;
    private Label[] opponentLabels;
    private ImageView[] opponentAvatars;

    // Variabile per stabilire il cambio della texture del deck in base all'era in cui ci si trova
    private int currentEra = 0;

    // Summary card state variables
    private boolean isFrontInfoCard = true;
    private List<Image> summaryCard = new ArrayList<>();

    @FXML
    public void initialize(VirtualServer server, ClientModel model) {
        this.server = server;
        this.model = model;
        model.registerObserver(this);
        myNickname = SceneManager.getNickname();

        setupHandBox();
        setupCenter();
        setupSummaryCard();
        if (model.getState() != null) {
            Platform.runLater(() -> {
                boardCenterController.update(model.getState());
                updateTokens(model.getState());
                playerTribeController.updateTribe(model.getState());
                updatePlayerInfo(model.getState());
            });
        }

        opponentBoxes = new VBox[]{ left_player1, right_player1, left_player2, right_player2 };
        opponentLabels = new Label[]{ labelLeft1, labelRight1, labelLeft2, labelRight2 };
        opponentAvatars = new ImageView[]{ avatarLeft1, avatarRight1, avatarLeft2, avatarRight2 };

        for (VBox b : opponentBoxes) b.setVisible(false);

        ClientGameState initialState = model.getState();
        if (initialState != null) {
            updatePlayerInfo(initialState);
        }
    }

    public void setDependencies(ImageCache cache) {
        this.imageCache = cache;
    }

    // Render an embedded version of player's tribe scene
    public void setupHandBox() {
        if (playerTribeController != null) {
            playerTribeController.setDependencies(this.imageCache);
            playerTribeController.setEmbeddedMode();
            playerTribeController.initialize(this.server, this.model, this.myNickname);
        }
    }

    // Render board center
    public void setupCenter() {
        if (boardCenterController != null) {
            boardCenterController.initialize(this.server, this.model, this.myNickname);
            boardCenterController.setDependencies(this.imageCache);
            boardCenterController.update(this.model.getState());
            boardCenterController.changeEra(1);
        }
    }

    // Render summary card
    private void setupSummaryCard() {
        summaryCardImage.setImage(this.imageCache.renderSummaryCard().getFirst());

        // Hover effect
        summaryCardContainer.setOnMouseEntered(e -> {
            summaryCardImage.setOpacity(0.6);
            flipIcon.setVisible(true);
        });

        // Unhover effect
        summaryCardContainer.setOnMouseExited(e -> {
            summaryCardImage.setOpacity(1.0);
            flipIcon.setVisible(false);
        });

        // Flip animation on mouse click
        summaryCardContainer.setOnMouseClicked(e -> flipSummaryCardAnimation());
    }

    @Override
    public void onStateUpdated(ClientGameState state) {
        Platform.runLater(() -> {
            boardCenterController.update(state);
            updateTokens(state);
            playerTribeController.updateTribe(state);
            updatePlayerInfo(state);
            //TODO if (this.currentEra != era) updateDeckEra(state.getCurrentEra);
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                return;
            }
            // updatePlayerInfo(state);
        });
    }

    private void updateDeckEra(int era) {
        this.currentEra = era;
        this.boardCenterController.changeEra(this.currentEra);
    }

    // ─────────────────────── Player Info & Tooltip ───────────────────────

    //TODO
    private void updatePlayerInfo(ClientGameState state) {
        Map<String, ClientPlayerState> players = state.getPlayers();

        // Usa prima i slot sinistri, poi quelli destri
        VBox[] containers = { left_player1, left_player2, right_player1, right_player2 };
        for (VBox b : containers) { b.getChildren().clear(); b.setVisible(false); }

        int slot = 0;
        for (ClientPlayerState player : players.values()) {
            if (player.getNickname().equals(myNickname)) continue;
            if (slot >= containers.length) break;

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/it/polimi/ingsw/am48/view/gui/opponent-widget.fxml")
                );
                Parent widget = loader.load();
                OpponentWidgetController wc = loader.getController();

                wc.setModel(model);
                wc.setServer(server);
                wc.setDependencies(imageCache);

                String totemColor = player.getTotemColor();
                Image avatarImg = (totemColor != null) ? imageCache.renderTotem(totemColor) : null;
                wc.setPlayerData(player.getNickname(), avatarImg);

                containers[slot].getChildren().add(widget);
                containers[slot].setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            slot++;
        }
    }

    private void openOpponentTribePopup(String nickname) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/polimi/ingsw/am48/view/gui/player-tribe-screen.fxml")
            );
            Parent root = loader.load();

            PlayerTribeController tc = loader.getController();
            tc.setModel(this.model);
            tc.setServer(this.server);
            tc.setDependencies(this.imageCache); // PlayerTribeController.setDependencies(ImageCache)
            tc.initialize(this.server, this.model, nickname);
            tc.updateTribe(this.model.getState());

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle(nickname + "'s Tribe");
            popup.setScene(new Scene(root));
            popup.setResizable(true);
            popup.setMinWidth(600);
            popup.setMinHeight(400);
            popup.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupPlayerTooltip(Label label, ClientPlayerState player) {
        Tooltip tooltip = new Tooltip();
        HBox container = new HBox(5);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #ffd700; -fx-border-width: 2;");

        addCardsToContainer(container, player.getCharacterCardIds(), 80);
        addCardsToContainer(container, player.getBuildingCardIds(), 80);

        tooltip.setGraphic(container);
        tooltip.setShowDelay(Duration.millis(100));
        label.setTooltip(tooltip);
    }

    //TODO da togliere (ci sono ancora dipendenze)
    private void addCardsToContainer(HBox container, List<String> ids, double height) {
        /*for (String id : ids) {
            ImageView img = createCardImageView(id, false);
            img.setFitHeight(height);
            container.getChildren().add(img);
        }*/
    }

    // ─────────────────────── Utilities & Handlers ───────────────────────

    private void updateTokens(ClientGameState state) {
        ClientPlayerState myState = state.getPlayer(myNickname);
        if (myState != null) {
            foodCount.setText(String.valueOf(myState.getFood()));
            prestigeCount.setText(String.valueOf(myState.getPoints()));
        }
    }

    @Override public void onError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("GAME ERROR");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // serve alla fine per passare al decimo turno alla scena successiva
    private void transitionToLeaderboard() {
        LeaderboardController lb = (LeaderboardController) SceneManager.changeScene("leaderboard.fxml");
        if (lb != null) {
            lb.setServer(server);
            lb.setModel(model);
        }
    }

    // ======================================= SUMMARY CARD ANIMATION ========================================

    private void flipSummaryCardAnimation() {
        // Closing animation
        ScaleTransition flipOut = new ScaleTransition(Duration.millis(150), summaryCardImage);
        flipOut.setFromX(1);
        flipOut.setToX(0);

        // Opening animation
        ScaleTransition flipIn = new ScaleTransition(Duration.millis(150), summaryCardImage);
        flipIn.setFromX(0);
        flipIn.setToX(1);

        // Switch images when the card is completely compressed
        flipOut.setOnFinished(event -> {
            this.isFrontInfoCard = !this.isFrontInfoCard;
            this.summaryCardImage.setImage(isFrontInfoCard ?
                    this.imageCache.renderSummaryCard().getFirst() :
                    this.imageCache.renderSummaryCard().getLast());

            // Animartion restart
            flipIn.play();
        });

        // Animation start
        flipOut.play();
    }
}