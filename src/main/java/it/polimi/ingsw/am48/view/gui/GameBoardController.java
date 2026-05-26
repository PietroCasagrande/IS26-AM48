package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.delta.EventInfo;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.view.CardDataRegistry;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.*;

public class GameBoardController implements ModelObserver {

    //Root
    @FXML private StackPane rootboard;

    // Bottom area
    @FXML private ScrollPane myHandBox;
    @FXML private PlayerTribeController playerTribeController;
    @FXML private Label prestigeCount;
    @FXML private ImageView prestige_points;
    @FXML private Label foodCount;
    @FXML private Button skipButton;
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

    // Phase / Turn indicator /Era indicator
    @FXML private HBox phaseIndicator;
    @FXML private Label phaseLabel;
    @FXML private Label turnLabel;
    @FXML private ImageView turnTotem;
    @FXML private Label turnNumberLabel;
    @FXML private Label eraLabel;

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

    // Event detection from lower row changes
    private List<String> prevLowerRowIds = new ArrayList<>();
    private boolean showingEvents = false;

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
        skipButton.setVisible(false);
        skipButton.setDisable(true);

        if (model.getState() != null) {
            Platform.runLater(() -> {
                boardCenterController.update(model.getState());
                updateTokens(model.getState());
                playerTribeController.updateTribe(model.getState());
                updatePlayerInfo(model.getState());
                updatePhaseInfo(model.getState());
                updateDeckEra(model.getState().getCurrEra());
            });
        }

        opponentBoxes = new VBox[]{ left_player1, right_player1, left_player2, right_player2 };
        opponentLabels = new Label[]{ labelLeft1, labelRight1, labelLeft2, labelRight2 };
        opponentAvatars = new ImageView[]{ avatarLeft1, avatarRight1, avatarLeft2, avatarRight2 };

        for (VBox b : opponentBoxes) b.setVisible(false);

        ClientGameState initialState = model.getState();
        if (initialState != null) {
            prevLowerRowIds = new ArrayList<>(initialState.getLowerRowCardIds());
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
            List<String> currentLower = state.getLowerRowCardIds();
            List<String> removedEventCards = new ArrayList<>();
            for (String id : prevLowerRowIds) {
                if (!currentLower.contains(id) && id.startsWith("EV")) {
                    removedEventCards.add(id);
                }
            }
            prevLowerRowIds = new ArrayList<>(currentLower);

            List<EventInfo> eventInfos = state.getEvents();
            if (eventInfos == null) eventInfos = new ArrayList<>();
            state.setEvents(new ArrayList<>());

            if (!removedEventCards.isEmpty()) {
                showEventNotifications(removedEventCards, eventInfos);
            }

            // cambia l'era sul tabellone centrale
            updateDeckEra(state.getCurrEra());
            checkExtraPick(state);
            checkAcquirableCharacters(state);

            boardCenterController.update(state);
            updateTokens(state);
            playerTribeController.updateTribe(state);
            updatePlayerInfo(state);
            updatePhaseInfo(state);
            playBoardSounds(state);
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                this.model.unregisterObserver(this);
                return;
            }
        });
    }

    private void updateDeckEra(int era) {
        this.currentEra = era + 1;
        this.boardCenterController.changeEra(this.currentEra);
    }

    @FXML
    private void handleSkipAction() {
        String myNickname = this.myNickname;
        try {
            server.takeCard(myNickname, "skip");
            skipButton.setDisable(true);

        } catch (Exception e) {
            System.err.println("Errore di connessione durante lo skip.");
        }
    }

    private void checkExtraPick(ClientGameState state) {
        String myNickname = SceneManager.getNickname();

        boolean hasSkipBuilding = state.getPlayer(myNickname).getBuildingCardIds().contains("BLD-21");
        boolean isCorrectPhase = "PLAYER_OFFER".equals(state.getCurrentPhase());
        boolean isMyTurn = state.getOfferTrackPositions().values().stream()
                .allMatch(value -> "".equals(value));

        if (hasSkipBuilding && isCorrectPhase && isMyTurn) {
            skipButton.setVisible(true);
            skipButton.setDisable(false);
        } else {
            skipButton.setVisible(false);
            skipButton.setDisable(true);
        }
    }

    private void checkAcquirableCharacters(ClientGameState state) {

        boolean isUpperEmpty = state.getUpperRowCardIds().stream()
                .allMatch(cardId -> cardId.startsWith("EV"));
        boolean isLowerEmpty = state.getLowerRowCardIds().stream()
                .allMatch(cardId -> cardId.startsWith("EV"));
        boolean isMyTurn = getCurrentPlayer(state).equals(myNickname);
        Character myOfferCard = state.getOfferTrackPositions()
                .entrySet()
                .stream()
                .filter(entry -> myNickname.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        boolean canSkip = false;

        if (isMyTurn && myOfferCard != null) {
            char card = myOfferCard;

            if (isUpperEmpty && (card == 'C' || card == 'E' || card == 'F' || card == 'G')) {
                canSkip = true;
            }
            else if (isLowerEmpty && (card == 'B' || card == 'D' || card == 'E' || card == 'G')) {
                canSkip = true;
            }
        }

        skipButton.setVisible(canSkip);
        skipButton.setDisable(!canSkip);

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
                wc.setPlayerStats(player.getFood(), player.getPoints());

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

    // ─────────────────────── Phase / Turn Indicator ───────────────────────

    private void updatePhaseInfo(ClientGameState state) {
        String rawPhase = state.getCurrentPhase();
        phaseLabel.setText("Phase: " + formatPhase(rawPhase));

        // AGGIORNAMENTO NUMERO TURNO ED ERA CORRENTE
        if (turnNumberLabel != null) {
            turnNumberLabel.setText("Round: " + state.getCurrentTurn());
        }
        if (eraLabel != null) {
            eraLabel.setText("Era: " + (state.getCurrEra() + 1));
        }

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
            prestige_points.setImage(imageCache.renderPrestige(myState.getPoints() < 0));
        }
    }

    @Override public void onError(String message) {
        Platform.runLater(() -> {
            this.soundCache.playFAAAAH();
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

    // ─────────────────────── Event notifications via lower-row detection ───────────────────────

    private void showEventNotifications(List<String> cardIds, List<EventInfo> eventInfos) {
        if (cardIds == null || cardIds.isEmpty() || showingEvents) return;
        showingEvents = true;
        showNextEvent(new ArrayList<>(cardIds), 0, eventInfos);
    }

    private void showNextEvent(List<String> cardIds, int index, List<EventInfo> eventInfos) {
        if (index >= cardIds.size()) {
            showingEvents = false;
            return;
        }

        String cardId = cardIds.get(index);
        String eventType = getEventTypeFromCardId(cardId);

        VBox notification = buildEventNotification(eventType);

        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        StackPane.setMargin(notification, new Insets(15, 0, 0, 0));
        rootboard.getChildren().add(notification);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), notification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
            pause.setOnFinished(ev -> fadeOut.play());
            pause.play();
        });

        int nextIndex = index + 1;
        fadeOut.setOnFinished(e -> {
            rootboard.getChildren().remove(notification);
            showNextEvent(cardIds, nextIndex, eventInfos);
        });

        fadeIn.play();
    }

    private VBox buildEventNotification(String eventType) {
        VBox notification = new VBox(6);
        notification.setAlignment(Pos.CENTER);
        notification.setMaxWidth(500);
        notification.setMaxHeight(100);
        notification.getStyleClass().add("event-notification");
        notification.setOpacity(0);

        String displayName = formatEventName(eventType);
        Label titleLabel = new Label(displayName);
        titleLabel.getStyleClass().add("event-title");

        notification.getChildren().add(titleLabel);

        return notification;
    }

    private String getEventTypeFromCardId(String cardId) {
        if (cardId.startsWith("EVA-")) return "ARTIST_EVENT";
        if (cardId.startsWith("EVH-")) return "HUNTER_EVENT";
        if (cardId.startsWith("EVS-")) return "SHAMAN_EVENT";
        if (cardId.startsWith("EVP-")) return "PICKER_EVENT";
        return "UNKNOWN_EVENT";
    }

    private String formatEventName(String raw) {
        return switch (raw) {
            case "ARTIST_EVENT"  -> "Artist Event";
            case "HUNTER_EVENT"  -> "Hunter Event";
            case "SHAMAN_EVENT"  -> "Shaman Event";
            case "PICKER_EVENT"  -> "Sustenance";
            default              -> raw;
        };
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

            // Animation restart
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