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

import java.util.*;

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

    // Phase / Turn indicator
    @FXML private HBox phaseIndicator;
    @FXML private Label phaseLabel;
    @FXML private Label turnLabel;
    @FXML private ImageView turnTotem;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;
    private CardDataRegistry cardData;
    private ImageCache imageCache;
    private SoundCache soundCache;

    private VBox[] opponentBoxes;
    private Label[] opponentLabels;
    private ImageView[] opponentAvatars;

    // Variabile per stabilire il cambio della texture del deck in base all'era in cui ci si trova
    private int currentEra = 0;

    // Summary card state variables
    private boolean isFrontInfoCard = true;
    private List<Image> summaryCard = new ArrayList<>();

    // Memorie globali nel tuo Controller
    private int previousTotemCount = 0;
    private Set<String> previousCardIds = new HashSet<>();

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
                updatePhaseInfo(model.getState());
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

    public void setDependencies(ImageCache imgCache, SoundCache soundCache) {
        this.imageCache = imgCache;
        this.soundCache = soundCache;
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
            updatePhaseInfo(state);
            playBoardSounds(state);
            //TODO if (this.currentEra != era) updateDeckEra(state.getCurrentEra);
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                this.model.unregisterObserver(this);
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
        String currentTurn = getCurrentPlayer(state);

        // Usa prima i slot sinistri, poi quelli destri
        VBox[] containers = { left_player1, left_player2, right_player1, right_player2 };
        for (VBox b : containers) { b.getChildren().clear(); b.setVisible(false); }

        String currentPhase = state.getCurrentPhase();
        boolean showTurnGlow = "PLACE_TOTEM".equals(currentPhase) || "PLAYER_OFFER".equals(currentPhase);

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

                if (showTurnGlow && player.getNickname().equals(currentTurn)) {
                    containers[slot].setStyle("-fx-effect: dropshadow(gaussian, #FFD700, 12, 0.6, 0, 0);");
                } else {
                    containers[slot].setStyle("");
                }
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

    // ─────────────────────── Phase / Turn Indicator ───────────────────────

    private void updatePhaseInfo(ClientGameState state) {
        String rawPhase = state.getCurrentPhase();
        phaseLabel.setText("Phase: " + formatPhase(rawPhase));

        String currentPlayer = getCurrentPlayer(state);
        if (currentPlayer != null) {
            turnLabel.setText("Turn: " + currentPlayer);

            ClientPlayerState p = state.getPlayer(currentPlayer);
            if (p != null) {
                String color = p.getTotemColor();
                Image totemImg = imageCache.renderTotem(color);
                turnTotem.setImage(totemImg);
                turnTotem.setVisible(true);
            } else {
                turnTotem.setVisible(false);
            }
        } else {
            turnLabel.setText("Turn: --");
            turnTotem.setVisible(false);
        }
    }

    private String getCurrentPlayer(ClientGameState state) {
        String phase = state.getCurrentPhase();
        if ("PLACE_TOTEM".equals(phase)) {
            List<String> order = state.getOfferTurnCardOrder();
            return (order != null && !order.isEmpty()) ? order.get(0) : null;
        } else if ("PLAYER_OFFER".equals(phase)) {
            return state.getOfferTrackPositions().entrySet().stream()
                    .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                    .min(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .orElse(null);
        }
        return null;
    }

    private String formatPhase(String phase) {
        return switch (phase) {
            case "WAITING_FOR_PLAYERS" -> "Waiting for Players";
            case "PLACE_TOTEM"         -> "Place Your Totem";
            case "PLAYER_OFFER"        -> "Player Offer";
            case "END_TURN"            -> "End Turn";
            case "END_GAME"            -> "Game Over";
            default                    -> phase;
        };
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

    // ======================================= BOARD SOUNDS ========================================

    private void playBoardSounds(ClientGameState state) {

        // Counting totems on track
        int currentTotemCount = (int) state.getOfferTrackPositions().values().stream()
                .filter(nick -> nick != null && !nick.trim().isEmpty())
                .count();

        // Saving cardIds to compute delta diff
        Set<String> currentCardIds = new HashSet<>();
        currentCardIds.addAll(state.getUpperRowCardIds());
        currentCardIds.addAll(state.getLowerRowCardIds());
        currentCardIds.addAll(state.getBuildingUpperIds());
        currentCardIds.addAll(state.getBuildingLowerIds());

        // Place Totem sound
        if (currentTotemCount > previousTotemCount) {
            this.soundCache.playTotem();
        }

        // Take Card sound
        boolean cardWasDrawn = false;
        for (String oldCardId : previousCardIds) {
            if (!currentCardIds.contains(oldCardId)) {
                cardWasDrawn = true;
                break; // just one card is necessary
            }
        }

        if (cardWasDrawn) {
            this.soundCache.playCard();
        }

        // Save state to compute diff in the next update
        previousTotemCount = currentTotemCount;
        previousCardIds = currentCardIds;
    }
}