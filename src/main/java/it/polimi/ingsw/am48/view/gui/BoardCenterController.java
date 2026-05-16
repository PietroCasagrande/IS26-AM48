package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;

public class BoardCenterController {

    //Root
    @FXML private GridPane boardCenterRoot;

    // Contenitori Righe Carte
    @FXML private HBox upperCharacterRow;
    @FXML private HBox upperBuildingRow;
    @FXML private HBox lowerCharacterRow;
    @FXML private HBox lowerBuildingRow;

    // Contenitori Riga Centrale
    @FXML private Pane offerTurnCard;
    @FXML private HBox offerTrackContainer;
    @FXML private VBox deckContainer;

    // Relative totem positions on OfferTurnCard
    private Map<String, Integer> totemSlotRegistry = new HashMap<>();
    private static final double[][] SLOT_CENTER_FRACTIONS = {
            {},
            {},
            {0.290, 0.470},                       // 2 players
            {0.240, 0.420, 0.600},                // 3 players
            {0.205, 0.380, 0.560, 0.735},         // 4 players
            {0.130, 0.310, 0.480, 0.650, 0.830}   // 5 players
    };

    private VirtualServer server;
    private ClientModel model;
    private ImageCache imageCache;
    private String myNickname;

    public void setDependencies(ImageCache cache) {
        this.imageCache = cache;
    }

    @FXML public void initialize(VirtualServer server, ClientModel model, String myNickname) {
        this.server = server;
        this.model = model;
        this.myNickname = myNickname;

        Platform.runLater(() -> {
            if (this.model != null && this.model.getState() != null) {
                int numPlayers = this.model.getState().getPlayers().size();
                setupOfferTurnCard(numPlayers);
                setupOfferTrack();
            } else {
                System.err.println("Errore critico: Model non inizializzato in tempo.");
            }
        });
    }

    // Render OfferTurnCard for the correct number of players
    //TODO sistema la generazione dell'immagine con cache (?)
    private void setupOfferTurnCard(int numPlayers) {

        try {
            String turnPath = "/it/polimi/ingsw/am48/view/gui/images/offerTurnCards/offerTurnCard" + numPlayers + ".png";
            Image offerTurnImage = new Image(getClass().getResourceAsStream(turnPath));
            if (!offerTurnImage.isError()) {
                BackgroundImage bgi = new BackgroundImage(
                        offerTurnImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                );
                offerTurnCard.setBackground(new Background(bgi));
            } else {
                System.err.println("Immagine turni non trovata per " + numPlayers + " giocatori.");
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento griglia turni: " + e.getMessage());
        }
    }

    // Render OfferCardTrack for the correct number of players
    //TODO sistema la generazione dell'immagine con cache
    private void setupOfferTrack(){

        Set<Character> offerCards = this.model.getState().getOfferTrackPositions().keySet();
        List<Character> sortedSlots = new ArrayList<>(offerCards);
        Collections.sort(sortedSlots);

        for(Character letter : sortedSlots){
            VBox slot = new VBox();
            slot.setUserData(letter);
            slot.setPrefSize(95.0, 180.0);
            slot.setAlignment(Pos.TOP_CENTER);
            slot.setPadding(new Insets(22, 0, 0, 0));

            String bgPath = "/it/polimi/ingsw/am48/view/gui/images/offerCards/offerCard" + letter + ".png";
            Image bgImage = new Image(getClass().getResourceAsStream(bgPath));

            if (!bgImage.isError()) {
                BackgroundImage bgi = new BackgroundImage(
                        bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                );
                slot.setBackground(new Background(bgi));
            }

            slot.setOnMouseClicked(e -> handleOfferTrackClick(letter));
            offerTrackContainer.getChildren().add(slot);
        }
    }

    // ====================================================================================================

    //TODO
    // --- METODI DI UPDATE (Chiamati ad ogni aggiornamento dal Server) ---
    public void update(ClientGameState state) {
        renderCards(upperCharacterRow, state.getUpperRowCardIds());
        renderCards(upperBuildingRow, state.getBuildingUpperIds());
        renderCards(lowerCharacterRow, state.getLowerRowCardIds());
        renderCards(lowerBuildingRow, state.getBuildingLowerIds());

        updateTotemGrid(state);
        //updateOfferTrackTotems(state);
        // updateTurnOrderCard(state); // Qui aggiornerai la griglia di sinistra
    }

    // Aggiorna dinamicamente una qualsiasi HBox con le carte del momento
    private void renderCards(HBox rowContainer, List<String> cardIds) {
        rowContainer.getChildren().clear();

        for (String cardId : cardIds) {
            // Usa sempre la cache!
            Image cardImage = imageCache.renderImage(cardId);
            if (cardImage != null) {
                ImageView imgView = new ImageView(cardImage);
                imgView.fitHeightProperty().bind(rowContainer.heightProperty().multiply(0.8));
                imgView.setPreserveRatio(true);
                imgView.getStyleClass().add("card-hover");

                // Click per pescare la carta
                imgView.setOnMouseClicked(e -> {
                    try { server.takeCard(myNickname, cardId); }
                    catch (Exception ex) { ex.printStackTrace(); }
                });

                rowContainer.getChildren().add(imgView);
            }
        }
    }

    // Update Deck Era
    public void changeEra(int era){
        if(this.deckContainer != null) deckContainer.getChildren().clear();

        String deckPath = "/it/polimi/ingsw/am48/view/gui/images/deckEra/deckEra" + era + ".png";
        Image deckImage = new Image(getClass().getResourceAsStream(deckPath));

        if (!deckImage.isError()) {
            ImageView deckView = new ImageView(deckImage);
            deckView.fitHeightProperty().bind(boardCenterRoot.heightProperty().multiply(0.35).multiply(0.8));
            deckView.setPreserveRatio(true);
            deckContainer.getChildren().add(deckView);
        } else {
            System.err.println("Immagine mazzo non trovata per Era: " + era + ". Path cercato: " + deckPath);
        }
    }

    //TODO
    // Sposta solo i totem sui 7 slot già preparati
    private void updateOfferTrackTotems(ClientGameState state) {
        Map<Character, String> positions = state.getOfferTrackPositions();

        // Iteriamo sui 7 slot che abbiamo creato nel setup
        for (int i = 0; i < 7; i++) {
            char letter = (char) ('A' + i);
            VBox slot = (VBox) offerTrackContainer.getChildren().get(i);

            // Rimuoviamo il totem del turno precedente
            slot.getChildren().clear();

            // Se c'è un giocatore su questa lettera, disegniamo il totem
            if (positions.containsKey(letter)) {
                String playerNick = positions.get(letter);
                // NOTA: Dovrai spostare la logica getTotemPath in una classe helper o nel Model
                String totemPath = getTotemPath(playerNick, state);

                Image totemImg = imageCache.renderImage(totemPath);
                if (totemImg != null) {
                    ImageView imgView = new ImageView(totemImg);
                    imgView.setFitHeight(50);
                    imgView.setPreserveRatio(true);
                    slot.getChildren().add(imgView);
                }
            }
        }
    }

    private void handleOfferTrackClick(char letter) {
        // La logica di click (controllare le fasi)
        try {
            this.server.placeTotem(this.myNickname, letter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateTotemGrid(ClientGameState state) {
        this.offerTurnCard.getChildren().clear();

        List<String> currentOrderList = state.getOfferTurnCardOrder();

        if (currentOrderList == null || currentOrderList.isEmpty()) {
            totemSlotRegistry.clear();
            return;
        }

        int numPlayers = state.getPlayers().size();
        double[] yPercentages = SLOT_CENTER_FRACTIONS[numPlayers];

        // on totem return during player offer phase
        for (String player : currentOrderList) {
            if (!totemSlotRegistry.containsKey(player)) {
                int freeSlot = totemSlotRegistry.size();
                totemSlotRegistry.put(player, freeSlot);
            }
        }

        // totem rendering
        for (String player : currentOrderList) {
            int slotIndex = totemSlotRegistry.get(player);

            String totemPath = getTotemPath(player, state);
            Image totemImage = new Image(getClass().getResourceAsStream(totemPath));
            ImageView totemImg = new ImageView(totemImage);
            totemImg.setPreserveRatio(true);
            totemImg.fitHeightProperty().bind(this.offerTurnCard.heightProperty().multiply(0.25));
            totemImg.layoutXProperty().bind(
                    this.offerTurnCard.widthProperty().multiply(0.381) // <-- GIOCA CON QUESTO NUMERO
                            .subtract(totemImg.fitWidthProperty().divide(2))
            );

            totemImg.layoutYProperty().bind(
                    this.offerTurnCard.heightProperty().multiply(yPercentages[slotIndex])
                            .subtract(totemImg.fitHeightProperty().divide(2))
            );

            DropShadow blackShadow = new DropShadow();
            blackShadow.setRadius(5.0); // Sfocatura
            blackShadow.setOffsetX(2.0); // Spostamento a destra
            blackShadow.setOffsetY(3.0); // Spostamento in basso
            blackShadow.setColor(Color.color(0, 0, 0, 0.7)); // Nero al 70% di opacità

// 2. Controllo se questo è il MIO totem
            if (player.equals(myNickname)) {
                // Creiamo un alone luminoso (Glow) giallo oro
                DropShadow yellowGlow = new DropShadow();
                yellowGlow.setRadius(15.0); // Molto sfocato per fare l'effetto "alone"
                yellowGlow.setOffsetX(0.0); // Centrato
                yellowGlow.setOffsetY(0.0); // Centrato
                yellowGlow.setColor(Color.web("#FFD700")); // Colore Gold / Giallo
                yellowGlow.setSpread(0.3); // Quanto è "densa" la luce (da 0.0 a 1.0)

                // TRUCCO PRO: In JavaFX un nodo può avere un solo "Effect".
                // Se vuoi ENTRAMBI gli effetti (alone giallo + ombra nera sotto),
                // devi "concatenarli" mettendo l'ombra nera come input del bagliore!
                yellowGlow.setInput(blackShadow);

                totemImg.setEffect(yellowGlow); // Applichiamo il super-effetto combinato

            } else {
                // Per gli avversari, applichiamo solo la normale ombra nera
                totemImg.setEffect(blackShadow);
            }

            totemImg.setId("totem_" + player); // ID per i click successivi

            this.offerTurnCard.getChildren().add(totemImg);
        }
    }

    private String getTotemPath(String nickname, ClientGameState state) {
        String color = state.getPlayer(nickname).getTotemColor();
        return "/it/polimi/ingsw/am48/view/gui/images/totems/" + color + "Totem.png";
    }
}
