package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.delta.EventInfo;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
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
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * Main controller for the game board scene.
 * It orchestrates all subcomponents (board center, player tribe, phase indicators, opponent widgets),
 * observes the client model for state updates, handles event notifications with animated popups,
 * manages the summary card flip animation, and plays contextual sound effects.
 * Implements {@link ModelObserver} to react to server state changes.
 */
public class GameBoardController implements ModelObserver {

    //Root
    @FXML private StackPane rootboard;

    // Bottom area
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
    @FXML private BoardCenterController boardCenterController;

    // Phase / Turn indicator /Era indicator
    @FXML private Label phaseLabel;
    @FXML private Label turnLabel;
    @FXML private ImageView turnTotem;
    @FXML private Label turnNumberLabel;
    @FXML private Label eraLabel;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;
    private ImageCache imageCache;
    private SoundCache soundCache;

    private VBox[] opponentBoxes;
    private Label[] opponentLabels;
    private ImageView[] opponentAvatars;
    private OpponentWidgetController[] opponentControllers = new OpponentWidgetController[4];

    // Tracks the current era to update the deck texture when the era changes
    private int currentEra = 0;

    // Event detection from lower row changes
    private List<String> prevLowerRowIds = new ArrayList<>();
    private boolean showingEvents = false;

    // Summary card state variables
    private boolean isFrontInfoCard = true;

    // Previous state values used to compute deltas for sound effects
    private int previousTotemCount = 0;
    private Set<String> previousCardIds = new HashSet<>();

    /**
     * Initializes the game board with the given server and model references.
     * Sets up all sub-controllers (hand box, board center, summary card),
     * registers as a model observer, and performs an initial render of the game state.
     * @param server the virtual server reference for sending commands
     * @param model the client model containing the current game state
     */
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
                playerTribeController.updateTribe();
                updatePlayerInfo(model.getState());
                updatePhaseInfo(model.getState());
                updateDeckEra(model.getState().getCurrEra());
            });
        }

        opponentBoxes = new VBox[]{ left_player1, right_player1, left_player2, right_player2 };
        opponentLabels = new Label[]{ labelLeft1, labelRight1, labelLeft2, labelRight2 };
        opponentAvatars = new ImageView[]{ avatarLeft1, avatarRight1, avatarLeft2, avatarRight2 };

        for (VBox b : opponentBoxes) b.setVisible(false);

        for (int i = 0; i < opponentBoxes.length; i++) {
            try {
                opponentBoxes[i].getChildren().clear();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/polimi/ingsw/am48/view/gui/opponent-widget.fxml"));
                Parent widget = loader.load();

                opponentControllers[i] = loader.getController();

                opponentControllers[i].setModel(model);
                opponentControllers[i].setServer(server);

                if (this.imageCache != null) {
                    opponentControllers[i].setDependencies(this.imageCache);
                }

                opponentBoxes[i].getChildren().add(widget);
                opponentBoxes[i].setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ClientGameState initialState = model.getState();
        if (initialState != null) {
            prevLowerRowIds = new ArrayList<>(initialState.getLowerRowCardIds());
            updatePlayerInfo(initialState);
        }
    }

    /**
     * Injects the image and sound cache dependencies.
     * @param imgCache the shared {@link ImageCache} instance
     * @param soundCache the shared {@link SoundCache} instance
     */
    public void setDependencies(ImageCache imgCache, SoundCache soundCache) {
        this.imageCache = imgCache;
        this.soundCache = soundCache;

        for (OpponentWidgetController wc : opponentControllers) {
            if (wc != null) {
                wc.setDependencies(imgCache);
            }
        }
    }

    /**
     * Renders an embedded version of the player's tribe scene inside the hand box area.
     * Delegates to {@link PlayerTribeController} with the cached dependencies.
     */
    public void setupHandBox() {
        if (playerTribeController != null) {
            playerTribeController.setDependencies(this.imageCache);
            playerTribeController.setEmbeddedMode();
            playerTribeController.initialize(this.server, this.model, this.myNickname);
        }
    }

    /**
     * Initializes and renders the board center sub-controller with the current game state.
     */
    public void setupCenter() {
        if (boardCenterController != null) {
            boardCenterController.initialize(this.server, this.model, this.myNickname);
            boardCenterController.setDependencies(this.imageCache);
            boardCenterController.update(this.model.getState());
            boardCenterController.changeEra(1);
        }
    }

    /**
     * Sets up the summary card in the bottom area with hover opacity effects,
     * a flip icon toggle, and a click handler that triggers the flip animation.
     */
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

    /**
     * Called by the client model whenever the game state is updated by the server.
     * Detects removed event cards to show animated notifications, checks for game end,
     * and refreshes all visual components on the JavaFX application thread.
     * @param state the updated client game state
     */
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
                showEventNotifications(removedEventCards);
            }

            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                this.model.unregisterObserver(this);
                return;
            }

            updateDeckEra(state.getCurrEra());
            checkExtraPick(state);
            checkAcquirableCharacters(state);
            boardCenterController.update(state);
            updateTokens(state);
            playerTribeController.updateTribe();
            updatePlayerInfo(state);
            updatePhaseInfo(state);
            playBoardSounds(state);
        });
    }

    /**
     * Updates the deck background image to match the current era.
     * @param era the current era index
     */
    private void updateDeckEra(int era) {
        this.currentEra = era + 1;
        this.boardCenterController.changeEra(this.currentEra);
    }

    /**
     * Handles the Skip action button. Sends a "skip" card take command to the server,
     * effectively allowing the player to forfeit their remaining picks.
     * Only enabled when the player holds building card BLD-21 and conditions are met.
     */
    @FXML
    private void handleSkipAction() {
        String myNickname = this.myNickname;
        try {
            server.takeCard(myNickname, "skip");
            skipButton.setDisable(true);

        } catch (Exception e) {
            System.err.println("Connection error during skip.");
        }
    }

    /**
     * Checks whether the local player has the BLD-21 building card that grants an extra pick.
     * If so, and it is the player's turn during PLAYER_OFFER with all totems placed,
     * the Skip button is shown and enabled.
     * @param state the current client game state
     */
    private void checkExtraPick(ClientGameState state) {
        String myNickname = SceneManager.getNickname();

        boolean hasSkipBuilding = state.getPlayer(myNickname).getBuildingCardIds().contains("BLD-21");
        boolean isCorrectPhase = "PLAYER_OFFER".equals(state.getCurrentPhase());
        boolean isMyTurn = state.getOfferTrackPositions().values().stream()
                .allMatch(""::equals);

        if (hasSkipBuilding && isCorrectPhase && isMyTurn) {
            skipButton.setVisible(true);
            skipButton.setDisable(false);
        } else {
            skipButton.setVisible(false);
            skipButton.setDisable(true);
        }
    }

    /**
     * Determines whether the current player can skip their turn during PLAYER_OFFER
     * based on which offer card they occupy and whether the target rows contain only event cards.
     * Enables or disables the Skip button accordingly.
     * @param state the current client game state
     */
    private void checkAcquirableCharacters(ClientGameState state) {

        if (skipButton.isVisible() && !skipButton.isDisabled()) {
            return;
        }

        boolean isUpperEmpty = state.getUpperRowCardIds().stream()
                .allMatch(cardId -> cardId.startsWith("EV"));
        boolean isLowerEmpty = state.getLowerRowCardIds().stream()
                .allMatch(cardId -> cardId.startsWith("EV"));
        boolean isMyTurn = Objects.equals(getCurrentPlayer(state), myNickname);
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

    /**
     * Updates the opponent widget panels on the sides of the board.
     * Each opponent is rendered using the {@code opponent-widget.fxml} layout,
     * showing their nickname, avatar (totem), food and prestige points.
     * The current turn player receives a golden glow effect during PLACE_TOTEM and PLAYER_OFFER phases.
     * @param state the current client game state
     */
    private void updatePlayerInfo(ClientGameState state) {
        Map<String, ClientPlayerState> players = state.getPlayers();
        String currentTurn = getCurrentPlayer(state);
        String currentPhase = state.getCurrentPhase();
        boolean showTurnGlow = "PLACE_TOTEM".equals(currentPhase) || "PLAYER_OFFER".equals(currentPhase);

        for (VBox b : opponentBoxes) { b.setVisible(false); }

        List<ClientPlayerState> opponents = new ArrayList<>();
        for (ClientPlayerState p : players.values()) {
            if (!p.getNickname().equals(myNickname)) {
                opponents.add(p);
            }
        }
        opponents.sort(Comparator.comparing(ClientPlayerState::getNickname));

        int slot = 0;
        for (ClientPlayerState player : opponents) {
            if (slot >= opponentBoxes.length) break;

            OpponentWidgetController wc = opponentControllers[slot];
            if (wc == null) { slot++; continue; }

            String totemColor = player.getTotemColor();
            Image avatarImg = (totemColor != null) ? imageCache.renderTotem(totemColor) : null;

            wc.setPlayerData(player.getNickname(), avatarImg);
            wc.setPlayerStats(player.getFood(), player.getPoints());

            opponentBoxes[slot].setVisible(true);

            if (showTurnGlow && player.getNickname().equals(currentTurn)) {
                opponentBoxes[slot].setStyle("-fx-effect: dropshadow(gaussian, #FFD700, 12, 0.6, 0, 0);");
            } else {
                opponentBoxes[slot].setStyle("");
            }
            slot++;
        }
    }

    // ─────────────────────── Phase / Turn Indicator ───────────────────────

    /**
     * Updates the phase indicator, turn label, round number, era indicator,
     * and the current player's totem icon in the top HUD area.
     * @param state the current client game state
     */
    private void updatePhaseInfo(ClientGameState state) {
        String rawPhase = state.getCurrentPhase();
        phaseLabel.setText("Phase: " + formatPhase(rawPhase));

        // updates round, turn and current era
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

    /**
     * Determines the nickname of the player whose turn it is.
     * During PLACE_TOTEM it is the first player in the turn order list.
     * During PLAYER_OFFER it is the player on the lowest-letter offer track slot.
     * @param state the current client game state
     * @return the nickname of the current turn player, or null if undetermined
     */
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

    /**
     * Converts an internal phase name into a human-readable display string.
     * @param phase the internal phase identifier
     * @return the formatted phase name for the UI
     */
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

    /**
     * Updates the local player's food and prestige point counters in the bottom HUD.
     * Also switches the prestige icon to the negative variant when points are below zero.
     * @param state the current client game state
     */
    private void updateTokens(ClientGameState state) {
        ClientPlayerState myState = state.getPlayer(myNickname);
        if (myState != null) {
            foodCount.setText(String.valueOf(myState.getFood()));
            prestigeCount.setText(String.valueOf(myState.getPoints()));
            prestige_points.setImage(imageCache.renderPrestige(myState.getPoints() < 0));
        }
    }

    /**
     * Called when the model reports an error. Displays an error alert dialog
     * and plays the error sound effect.
     * @param message the error description
     */
    @Override public void onError(String message) {
        Platform.runLater(() -> {
            this.soundCache.playFAAAAH();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("GAME ERROR");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setMinWidth(700);
            alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            alert.showAndWait();
        });
    }

    /**
     * Transitions from the game board to the leaderboard scene when the game ends.
     * Passes the server and model references to the leaderboard controller.
     */
    private void transitionToLeaderboard() {
        LeaderboardController lb = (LeaderboardController) SceneManager.changeScene("leaderboard.fxml");
        if (lb != null) {
            lb.setServer(server);
            lb.setModel(model);
        }
    }

    // ─────────────────────── Event notifications via lower-row detection ───────────────────────

    /**
     * Begins a sequential display of event notification popups for the given event card IDs.
     * Prevents overlapping notifications by checking the {@link #showingEvents} flag.
     * @param cardIds the list of event card IDs that were removed and should be notified
     */
    private void showEventNotifications(List<String> cardIds) {
        if (cardIds == null || cardIds.isEmpty() || showingEvents) return;
        showingEvents = true;
        showNextEvent(new ArrayList<>(cardIds), 0);
    }

    /**
     * Recursively shows event notification popups one at a time with a fade-in, pause, and fade-out animation.
     * When all events have been shown, resets the {@link #showingEvents} flag.
     * @param cardIds the list of remaining event card IDs to display
     * @param index the current index in the list
     */
    private void showNextEvent(List<String> cardIds, int index) {
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
            showNextEvent(cardIds, nextIndex);
        });

        fadeIn.play();
    }

    /**
     * Builds a styled VBox notification node for the given event type.
     * The notification is initially invisible (opacity 0) and will be animated later.
     * @param eventType the raw event type identifier
     * @return a styled VBox ready for fade-in animation
     */
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

    /**
     * Extracts the event type from an event card ID based on its prefix.
     * @param cardId the event card identifier
     * @return the corresponding event type string (e.g. "ARTIST_EVENT", "HUNTER_EVENT")
     */
    private String getEventTypeFromCardId(String cardId) {
        if (cardId.startsWith("EVA-")) return "ARTIST_EVENT";
        if (cardId.startsWith("EVH-")) return "HUNTER_EVENT";
        if (cardId.startsWith("EVS-")) return "SHAMAN_EVENT";
        return "PICKER_EVENT";}

    /**
     * Converts an internal event type name into a human-readable display string.
     * @param raw the raw event type identifier
     * @return the formatted event name for the notification UI
     */
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

    /**
     * Animates a horizontal flip of the summary card.
     * The card scales to zero width on the X axis, switches the image between front and back,
     * and then scales back to full width.
     */
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

    /**
     * Plays contextual sound effects by comparing the previous and current game state.
     * A totem placement sound is played when the number of placed totems increases.
     * A card draw sound is played when a card ID disappears from the board.
     * @param state the current client game state
     */
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