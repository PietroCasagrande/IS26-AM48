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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameBoardController implements ModelObserver {

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

    // Ordine iniziale completo dei totem (per mantenere slot fissi quando alcuni vengono rimossi)
    private List<String> initialTotemOrder;

    // Ordine colori assegnati in base all'ingresso
    private final String[] totemColors = {"blue", "red", "black", "white", "yellow"};


    // MODIFICA QUI per ajustare la posizione verticale di ogni totem:
    private static final double[][] SLOT_CENTER_FRACTIONS = {
            {},
            {},
            {0.311, 0.451},                       // 2 players
            {0.260, 0.435, 0.600},                // 3 players
            {0.222, 0.397, 0.570, 0.710},         // 4 players
            {0.146, 0.320, 0.494, 0.667, 0.807}   // 5 players
    };

    @FXML
    public void initialize() {
        myNickname = SceneManager.getNickname();

        offerCards = new VBox[]{ offerCardA, offerCardB, offerCardC, offerCardD, offerCardE, offerCardF, offerCardG };
        opponentBoxes = new VBox[]{ left_player, right_player, topLeft_player, topRight_player };
        opponentLabels = new Label[]{ labelLeft, labelRight, labelTopLeft, labelTopRight };
        opponentAvatars = new ImageView[]{ avatarLeft, avatarRight, avatarTopLeft, avatarTopRight };

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
            if (state.getWinnerNickname() != null) {
                transitionToLeaderboard();
                return;
            }
            if (myNickname == null) myNickname = SceneManager.getNickname();

            int numPlayers = state.getPlayers().size();
            // Aggiorna la board solo se il numero di giocatori cambia (o al primo caricamento)
            if (numPlayers > 0 && this.currentPlayerCount != numPlayers) {
                updateBoardConfiguration(numPlayers);
            }

            // updateDeckEra(state.getCurrentEra()); DA GESTIRE!!!
            updatePlayerInfo(state);
            updateBoardRows(state);
            updateOfferCards(state);
            updateTotemGrid(state);
            updateMyHand(state);
            updateTokens(state);
        });
    }

    private void updateDeckEra(int era) {
        if (this.currentEra == era) return; // Evita di riapplicare lo stile se l'era non è cambiata
        this.currentEra = era;

        // Rimuove le classi precedenti per evitare conflitti
        deck.getStyleClass().removeAll("deck-era1", "deck-era2", "deck-era3");

        // Aggiunge la classe corrispondente all'era
        switch (era) {
            case 1 -> deck.getStyleClass().add("deck-era1");
            case 2 -> deck.getStyleClass().add("deck-era2");
            case 3 -> deck.getStyleClass().add("deck-era3");
        }
    }

    public void setupHandBox() {
        if (playerTribeController != null) {
            playerTribeController.setServer(this.server);
            playerTribeController.setModel(this.model);
            playerTribeController.setDependencies(this.cardData, this.imageCache);

            playerTribeController.setEmbeddedMode();
            playerTribeController.initialize(this.myNickname);
        }
        else System.out.println("errore");

        // TODO Fai lo stesso per le altre carte della Board
    }

    // ─────────────────────── Helper per Colori Totem ───────────────────────

    private String getTotemPath(String nickname, ClientGameState state) {
        // Otteniamo la lista dei nickname per trovare l'indice di ingresso
        List<String> entryOrder = state.getPlayers().values().stream()
                .map(ClientPlayerState::getNickname)
                .toList();

        int index = entryOrder.indexOf(nickname);
        if (index < 0 || index >= totemColors.length) return "";

        String color = totemColors[index];
        return "/it/polimi/ingsw/am48/view/gui/images/totems/" + color + "Totem.png";
    }

    // ─────────────────────── Configurazioni Dinamiche Board ───────────────────────

    private double computeDisplayedH(Image img, double fitWidth) {
        if (img == null || img.isError() || img.getWidth() == 0) return 180;
        return img.getHeight() * (fitWidth / img.getWidth());
    }

    private void updateBoardConfiguration(int numPlayers) {
        this.currentPlayerCount = numPlayers;
        this.initialTotemOrder = null;

        String gridImagePath = "/it/polimi/ingsw/am48/view/gui/images/offerTurnCards/offerTurnCard" + numPlayers + ".png";
        try {
            Image bgImage = new Image(getClass().getResourceAsStream(gridImagePath));
            if (!bgImage.isError()) {
                BackgroundImage bgi = new BackgroundImage(
                        bgImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(95, computeDisplayedH(bgImage, 95), false, false, false, false)                );
                offerTurnCard.setBackground(new Background(bgi));
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento griglia turni: " + gridImagePath);
        }

        // Gestione visibilità slot offerta in base alle regole di Mesos
        offerCardA.setVisible(numPlayers == 5);
        offerCardA.setManaged(numPlayers == 5);

        offerCardD.setVisible(numPlayers >= 3);
        offerCardD.setManaged(numPlayers >= 3);

        offerCardG.setVisible(numPlayers >= 4);
        offerCardG.setManaged(numPlayers >= 4);
    }

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

            String totemPath = getTotemPath(player.getNickname(), state);
            try {
                avatar.setImage(new Image(getClass().getResourceAsStream(totemPath)));
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
                String path = getTotemPath(playerNick, state);

                try {
                    totemOnTrack.setImage(new Image(getClass().getResourceAsStream(path)));
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