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
    @FXML private VBox left_player, right_player, topLeft_player, topRight_player;
    @FXML private Label labelLeft, labelRight, labelTopLeft, labelTopRight;
    @FXML private ImageView avatarLeft, avatarRight, avatarTopLeft, avatarTopRight;

    // Board center
    @FXML private GridPane boardCenter;
    @FXML private VBox center;
    @FXML private BoardCenterController boardCenterController;
    @FXML private HBox upper_row, lower_row;
    @FXML private HBox deckable_sup, building_sup, deckable_inf, building_inf;
    @FXML private Pane offerTurnCard;
    @FXML private VBox offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG;
    @FXML private VBox deck;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;
    private CardDataRegistry cardData;
    private ImageCache imageCache;

    private VBox[] offerCards;
    private VBox[] opponentBoxes;
    private Label[] opponentLabels;
    private ImageView[] opponentAvatars;

    // Variabili di stato per la meccanica del Totem e configurazione board
    private boolean isMyTotemSelected = false;
    private int currentPlayerCount = 0;

    // Variabile per stabilire il cambio della texture del deck in base all'era in cui ci si trova
    private int currentEra = 0;

    @FXML
    public void initialize(VirtualServer server, ClientModel model) {
        this.server = server;
        this.model = model;
        model.registerObserver(this);
        if (model.getState() != null) onStateUpdated(model.getState());

        myNickname = SceneManager.getNickname();

        setupHandBox();
        setupCenter();

        //TODO
        //offerCards = new VBox[]{ offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG };
        opponentBoxes = new VBox[]{ left_player, right_player, topLeft_player, topRight_player };
        opponentLabels = new Label[]{ labelLeft, labelRight, labelTopLeft, labelTopRight };
        opponentAvatars = new ImageView[]{ avatarLeft, avatarRight, avatarTopLeft, avatarTopRight };

        for (VBox b : opponentBoxes) b.setVisible(false);

        //setupOfferCardClickHandlers();
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
            //TODO if (this.currentEra == era) updateDeckEra(state.getCurrentEra);
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                return;
            }

            /*
            updatePlayerInfo(state);
            updateBoardRows(state);
            updateOfferCards(state);
            updateTotemGrid(state);
            updateMyHand(state);
            updateTokens(state);*/
        });
    }

    private void updateDeckEra(int era) {
        this.currentEra = era;
        this.boardCenterController.changeEra(this.currentEra);
    }

    // ─────────────────────── Configurazioni Dinamiche Board ───────────────────────

    private double computeDisplayedH(Image img, double fitWidth) {
        if (img == null || img.isError() || img.getWidth() == 0) return 180;
        return img.getHeight() * (fitWidth / img.getWidth());
    }

    /* TODO messo in board center
    private void updateTotemGrid(ClientGameState state) {
        offerTurnCard.getChildren().clear();

        List<String> orderList = state.getOfferTurnCardOrder();
        if (orderList == null || orderList.isEmpty()) return;

        if (initialTotemOrder == null) {
            initialTotemOrder = new ArrayList<>(orderList);
        }

        String currentPhase = state.getCurrentPhase();
        boolean isPlaceTotemPhase = "PLACE_TOTEM".equals(currentPhase);
        boolean isMyTurn = isPlaceTotemPhase
                && !orderList.isEmpty()
                && myNickname.equals(orderList.get(0));

        int numSlots = currentPlayerCount > 0 ? currentPlayerCount : orderList.size();

        // Calcola l'altezza reale dell'immagine quando scalata a 95px di larghezza
        String gridImagePath = "/it/polimi/ingsw/am48/view/gui/images/offerTurnCards/offerTurnCard" + numSlots + ".png";
        double displayedH = 180.0; // fallback
        try {
            Image gridImg = new Image(getClass().getResourceAsStream(gridImagePath));
            if (!gridImg.isError()) {
                displayedH = computeDisplayedH(gridImg, 95);
            }
        } catch (Exception ignored) {}

        double vboxH = 180.0; // HBox.fillHeight=true → il VBox è sempre 180px
        double topOffset = Math.max(0, (vboxH - displayedH) / 2.0);
        double slotH = displayedH / numSlots;

        double[] slotFractions = (numSlots >= 2 && numSlots <= 5) ? SLOT_CENTER_FRACTIONS[numSlots] : null;
        double totemSize = slotH * 0.72;

        for (int i = 0; i < numSlots; i++) {
            StackPane slotPane = new StackPane();
            slotPane.setAlignment(Pos.CENTER);

            double slotCenterY;
            if (slotFractions != null && i < slotFractions.length) {
                slotCenterY = topOffset + slotFractions[i] * displayedH;
            } else {
                slotCenterY = topOffset + slotH * (i + 0.5);
            }

            double slotSize = totemSize * 1.10;
            slotPane.setPrefHeight(slotSize);
            slotPane.setMinHeight(slotSize);
            slotPane.setMaxHeight(slotSize);

            slotPane.setPrefWidth(95);
            slotPane.setMinWidth(95);
            slotPane.setMaxWidth(95);

            slotPane.setLayoutY(slotCenterY - slotSize / 2.0);

            // Ogni giocatore ha uno slot fisso in base all'ordine iniziale,
            // anche dopo che alcuni totem sono stati rimossi
            String nick = null;
            if (initialTotemOrder != null && i < initialTotemOrder.size()) {
                String slotPlayer = initialTotemOrder.get(i);
                if (orderList.contains(slotPlayer)) {
                    nick = slotPlayer;
                }
            }

            if (nick != null) {
                ImageView totemImg = new ImageView();
                try {
                    String path = getTotemPath(nick, state);
                    if (!path.isEmpty())
                        totemImg.setImage(new Image(getClass().getResourceAsStream(path)));
                } catch (Exception e) {
                    System.err.println("Impossibile caricare totem: " + nick);
                }
                totemImg.setPreserveRatio(true);
                totemImg.setFitHeight(totemSize);

                if (nick.equals(myNickname) && isMyTurn) {
                    totemImg.getStyleClass().add("totem-selectable");
                    totemImg.setOnMouseClicked(e -> {
                        isMyTotemSelected = true;
                        totemImg.setStyle("-fx-effect: dropshadow(gaussian, #ffd700, 15, 0.5, 0, 0);");
                    });
                }
                slotPane.getChildren().add(totemImg);
            }
            offerTurnCard.getChildren().add(slotPane);
        }
    }
    */

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
        for (String id : ids) {
            ImageView img = createCardImageView(id, false);
            img.setFitHeight(height);
            container.getChildren().add(img);
        }
    }

    private void updateOfferCards(ClientGameState state) {
        Map<Character, String> positions = state.getOfferTrackPositions();
        for (int i = 0; i < 7; i++) {
            char letter = (char) ('A' + i);
            VBox offerBox = offerCards[i];
            offerBox.getChildren().clear();

            String bgPath = "/it/polimi/ingsw/am48/view/gui/images/offerCards/offerCard" + letter + ".png";
            try {
                Image bgImage = new Image(getClass().getResourceAsStream(bgPath));
                if (!bgImage.isError()) {
                    BackgroundImage bgi = new BackgroundImage(
                            bgImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                    );
                    offerBox.setBackground(new Background(bgi));
                }
            } catch (Exception e) {
                System.err.println("Errore caricamento offer card: " + bgPath);
            }

            if (positions.containsKey(letter)) {
                String playerNick = positions.get(letter);
                ImageView totemOnTrack = new ImageView();
                //TODO String path = getTotemPath(playerNick, state);

                try {
                    //TODO totemOnTrack.setImage(new Image(getClass().getResourceAsStream(path)));
                    totemOnTrack.setFitHeight(50);
                    totemOnTrack.setPreserveRatio(true);
                    offerBox.setAlignment(Pos.TOP_CENTER);
                    offerBox.setPadding(new Insets(22, 0, 0, 0));
                    offerBox.getChildren().add(totemOnTrack);
                } catch (Exception e) {
                    Label lbl = new Label(playerNick);
                    lbl.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
                    offerBox.setAlignment(Pos.TOP_CENTER);
                    offerBox.setPadding(new Insets(22, 0, 0, 0));
                    offerBox.getChildren().add(lbl);
                }
            }
        }
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

    private ImageView createCardImageView(String cardId, boolean clickable) {
        ImageView img = new ImageView();
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
            img.getStyleClass().add("card-hover");
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
        if (server == null || model == null || model.getState() == null) return;

        if (!"PLACE_TOTEM".equals(model.getState().getCurrentPhase())) {
            System.out.println("Azione non consentita in questa fase!");
            return;
        }

        if (!isMyTotemSelected) {
            System.out.println("Seleziona prima il tuo totem dalla griglia!");
            return;
        }

        try {
            server.placeTotem(myNickname, letter);
            isMyTotemSelected = false;
        } catch (Exception ex) {
            ex.printStackTrace();
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