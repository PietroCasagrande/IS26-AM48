package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

    // ================================================ INITIAL SETUP ====================================================

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

    // =============================================== UPDATES =====================================================


    // Update board (called whenever server updates its state)
    public void update(ClientGameState state) {
        renderCards(upperCharacterRow, state.getUpperRowCardIds());
        renderCards(upperBuildingRow, state.getBuildingUpperIds());
        renderCards(lowerCharacterRow, state.getLowerRowCardIds());
        renderCards(lowerBuildingRow, state.getBuildingLowerIds());

        updateTotemGrid(state);
        updateTotemTrack(state);
    }

    // Update totem placement on offer turn card
    private void updateTotemGrid(ClientGameState state) {
        this.offerTurnCard.getChildren().clear();

        List<String> currentOrderList = state.getOfferTurnCardOrder();

        // Check empty list
        if (currentOrderList == null || currentOrderList.isEmpty()) {
            return;
        }

        int numPlayers = state.getPlayers().size();
        double[] yPercentages = SLOT_CENTER_FRACTIONS[numPlayers];

        // Check if current phase is PLACE TOTEM (emptying) or PLAYER OFFER (filling)
        String currentPhase = state.getCurrentPhase();
        boolean isEmptyingPhase = "PLACE_TOTEM".equals(currentPhase);

        for (int i = 0; i < currentOrderList.size(); i++) {
            String player = currentOrderList.get(i);
            int slotIndex;
            if (isEmptyingPhase) slotIndex = numPlayers - currentOrderList.size() + i;
            else slotIndex = i;

            if (slotIndex < 0 || slotIndex >= yPercentages.length) continue;

            ImageView totemImg = renderTotem(player, state);
            totemImg.fitHeightProperty().bind(this.offerTurnCard.heightProperty().multiply(0.25));

            totemImg.layoutXProperty().bind(
                    this.offerTurnCard.widthProperty().multiply(0.381)
                            .subtract(totemImg.fitWidthProperty().divide(2))
            );

            totemImg.layoutYProperty().bind(
                    this.offerTurnCard.heightProperty().multiply(yPercentages[slotIndex])
                            .subtract(totemImg.fitHeightProperty().divide(2))
            );

            this.offerTurnCard.getChildren().add(totemImg);
        }
    }

    // Update totem placement on offer card track
    private void updateTotemTrack(ClientGameState state) {
        Map<Character, String> positions = state.getOfferTrackPositions();

        for (Node node : offerTrackContainer.getChildren()) {
            if (node instanceof VBox slot) {

                slot.getChildren().clear();

                Character letter = (Character) slot.getUserData();
                if (letter != null && positions.containsKey(letter)) {
                    String playerNick = positions.get(letter);

                    if(playerNick != null && !playerNick.trim().isEmpty() && state.getPlayer(playerNick) != null){

                        ImageView totemImg = renderTotem(playerNick, state);
                        // --- BINDING RESPONSIVI ---
                        // Il totem scala seguendo il 30% dell'altezza dello slot
                        totemImg.fitHeightProperty().bind(slot.heightProperty().multiply(0.30));

                        // Spingiamo il totem in basso (es. 15%) per metterlo al centro del riquadro.
                        // Puoi aumentare/diminuire questo valore per tarare l'altezza!
                        totemImg.translateYProperty().bind(slot.heightProperty().multiply(0.0));

                        // Aggiungiamo il totem al VBox!
                        slot.getChildren().add(totemImg);
                    }
                }
            }
        }
    }

    private void handleOfferTrackClick(char letter) {
        try {
            this.server.placeTotem(this.myNickname, letter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleCardClick(String cardId) {
        if (server != null) {
            try { server.takeCard(myNickname, cardId); }
            catch (Exception ex) { ex.printStackTrace(); }
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

    // =============================================== IMAGE RENDERING =====================================================

    // Render Totem Image adding shadows
    private ImageView renderTotem(String nickname, ClientGameState state) {
        String color = state.getPlayer(nickname).getTotemColor();
        Image totem = this.imageCache.renderTotem(color);
        ImageView totemImg = new ImageView(totem);
        totemImg.setPreserveRatio(true);

        // Black shadow
        DropShadow blackShadow = new DropShadow();
        blackShadow.setRadius(5.0);
        blackShadow.setOffsetX(2.0);
        blackShadow.setOffsetY(3.0);
        blackShadow.setColor(Color.color(0, 0, 0, 0.7));

        // Yellow shadow for your own totem
        if (nickname.equals(myNickname)) {
            DropShadow yellowGlow = new DropShadow();
            yellowGlow.setRadius(15.0);
            yellowGlow.setOffsetX(0.0);
            yellowGlow.setOffsetY(0.0);
            yellowGlow.setColor(Color.web("#FFD700"));
            yellowGlow.setSpread(0.3);
            yellowGlow.setInput(blackShadow);
            totemImg.setEffect(yellowGlow);
        } else {
            totemImg.setEffect(blackShadow);
        }

        totemImg.setId("totem_" + nickname);

        return totemImg;
    }

    // Aggiorna dinamicamente una qualsiasi HBox con le carte del momento
    private void renderCards(HBox rowContainer, List<String> cardIds) {
        rowContainer.getChildren().clear();

        for (String cardId : cardIds) {
            // Usa sempre la cache!
            Image cardImage = imageCache.renderCards(cardId);
            if (cardImage != null) {
                ImageView imgView = new ImageView(cardImage);
                imgView.fitHeightProperty().bind(rowContainer.heightProperty().multiply(0.8));
                imgView.setPreserveRatio(true);
                imgView.getStyleClass().add("card-hover");

                // Click per pescare la carta
                imgView.setOnMouseClicked(e -> handleCardClick(cardId));

                rowContainer.getChildren().add(imgView);
            }
        }
    }
}
