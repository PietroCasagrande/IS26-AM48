package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.view.CardDataRegistry;
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
import javafx.scene.layout.*;
import javafx.util.Duration;

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

    private VBox[] offerCards;
    private VBox[] opponentBoxes;
    private Label[] opponentLabels;
    private ImageView[] opponentAvatars;

    // Variabile per stabilire il cambio della texture del deck in base all'era in cui ci si trova
    private int currentEra = 0;

    @FXML
    public void initialize(VirtualServer server, ClientModel model) {
        this.server = server;
        this.model = model;
        model.registerObserver(this);
        myNickname = SceneManager.getNickname();

        setupHandBox();
        setupCenter();

        //TODO
        //offerCards = new VBox[]{ offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG };
        opponentBoxes = new VBox[]{ left_player1, right_player1, left_player2, right_player2 };
        opponentLabels = new Label[]{ labelLeft1, labelRight1, labelLeft2, labelRight2 };
        opponentAvatars = new ImageView[]{ avatarLeft1, avatarRight1, avatarLeft2, avatarRight2 };

        for (VBox b : opponentBoxes) b.setVisible(false);
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

    //TODO =========================================================================================================
    @Override
    public void onStateUpdated(ClientGameState state) {
        Platform.runLater(() -> {
            boardCenterController.update(state);
            updateTokens(state);
            playerTribeController.updateTribe(state);
            //TODO if (this.currentEra != era) updateDeckEra(state.getCurrentEra);
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                return;
            }

            /*
            updatePlayerInfo(state);
            updateMyHand(state); */
        });
    }

    private void updateDeckEra(int era) {
        this.currentEra = era;
        this.boardCenterController.changeEra(this.currentEra);
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
            ImageView avatar = opponentAvatars[slot];

            label.setText(player.getNickname());
            box.setVisible(true);

            //TODO String totemPath = getTotemPath(player.getNickname(), state);
            try {
                //TODO avatar.setImage(new Image(getClass().getResourceAsStream(totemPath)));
                avatar.setFitHeight(60);
                avatar.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Impossibile caricare avatar per " + player.getNickname());
            }

            setupPlayerTooltip(label, player);
            slot++;
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

    private void addCardsToContainer(HBox container, List<String> ids, double height) {
        /*for (String id : ids) {
            ImageView img = createCardImageView(id, false);
            img.setFitHeight(height);
            container.getChildren().add(img);
        }*/
    }

    // ─────────────────────── Utilities & Handlers ───────────────────────

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

    @Override public void onError(String message) { System.err.println("Error: " + message); }

    // serve alla fine per passare al decimo turno alla scena successiva
    private void transitionToLeaderboard() {
        LeaderboardController lb = (LeaderboardController) SceneManager.changeScene("leaderboard.fxml");
        if (lb != null) {
            lb.setServer(server);
            lb.setModel(model);
        }
    }
}