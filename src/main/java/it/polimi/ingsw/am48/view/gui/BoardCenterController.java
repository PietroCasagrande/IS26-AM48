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
import javafx.scene.Cursor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the central area of the game board.
 * Manages the rendering of the offer turn card, the offer card track, the deck era indicator,
 * and all four card rows (upper/lower character and building rows).
 * Provides interactivity for the {@code PLACE_TOTEM} and {@code PLAYER_OFFER} phases,
 * allowing the current player to click on offer slots or cards to perform actions.
 */
public class BoardCenterController {

    //Root
    @FXML private GridPane boardCenterRoot;

    // Card row containers
    @FXML private HBox upperCharacterRow;
    @FXML private HBox upperBuildingRow;
    @FXML private HBox lowerCharacterRow;
    @FXML private HBox lowerBuildingRow;

    // Central row containers
    @FXML private Pane offerTurnCard;
    @FXML private HBox offerTrackContainer;
    @FXML private VBox deckContainer;

    // Relative totem positions on OfferTurnCard
    private Map<Character, VBox> renderedOfferSlots = new HashMap<>();
    private Map<String, StackPane> renderedOfferCards = new HashMap<>();
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

    /**
     * Injects the image cache dependency used to render card and totem images.
     * @param cache the shared {@link ImageCache} instance
     */
    public void setDependencies(ImageCache cache) {
        this.imageCache = cache;
    }

    /**
     * Initializes the board center controller with server, model and player identity.
     * Sets up the offer turn card background and the offer track once the model state is available.
     * @param server the virtual server reference for sending commands
     * @param model the client model containing the current game state
     * @param myNickname the nickname of the local player
     */
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
                System.err.println("Critical error: Model not initialized in time.");
            }
        });
    }

    /**
     * Renders the background image of the offer turn card, which displays turn order slots.
     * The image varies depending on the number of players in the game.
     * @param numPlayers the number of players in the current game.
     */
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
                System.err.println("Turn image not found for " + numPlayers + " players.");
            }
        } catch (Exception e) {
            System.err.println("Error loading turn grid: " + e.getMessage());
        }
    }

    /**
     * Renders the offer card track with slot backgrounds for each letter (A-G).
     * Each slot is a clickable VBox; the slots are sorted alphabetically and stored
     * in {@link #renderedOfferSlots} for interactivity during the PLACE_TOTEM phase.
     */
    private void setupOfferTrack(){

        Set<Character> offerCards = this.model.getState().getOfferTrackPositions().keySet();
        List<Character> sortedSlots = new ArrayList<>(offerCards);
        Collections.sort(sortedSlots);

        offerTrackContainer.getChildren().clear();
        renderedOfferSlots.clear();

        for(Character letter : sortedSlots){
            VBox slot = new VBox();
            slot.setUserData(letter);
            slot.setPrefSize(95.0, 180.0);
            slot.setAlignment(Pos.TOP_CENTER);
            slot.setPadding(new Insets(22, 0, 0, 0));

            String bgPath = "/it/polimi/ingsw/am48/view/gui/images/offerCards/offerCard" + letter + ".png";
            Image bgImage = new Image(getClass().getResource(bgPath).toExternalForm(), true);

            if (!bgImage.isError()) {
                BackgroundImage bgi = new BackgroundImage(
                        bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                );
                slot.setBackground(new Background(bgi));
            }

            offerTrackContainer.getChildren().add(slot);

            // Saving OfferCard to enlighten up them during PLACE TOTEM phase
            renderedOfferSlots.put(letter, slot);
        }
    }

    // =============================================== UPDATES =====================================================


    /**
     * Updates the entire board center to reflect the latest game state.
     * Re-renders all card rows, the totem grid on the offer turn card, the totem track,
     * and applies highlight interactivity based on the current game phase.
     * @param state the current client game state snapshot
     */
    public void update(ClientGameState state) {
        renderedOfferCards.clear();

        renderCards(upperCharacterRow, state.getUpperRowCardIds());
        renderCards(upperBuildingRow, state.getBuildingUpperIds());
        renderCards(lowerCharacterRow, state.getLowerRowCardIds());
        renderCards(lowerBuildingRow, state.getBuildingLowerIds());

        updateTotemGrid(state);
        updateTotemTrack(state);

        if(state.getCurrentPhase().equals("PLACE_TOTEM")){
            Map<Character, String> places = state.getOfferTrackPositions();
            List<Character> freeSlot = places.entrySet().stream()
                    .filter(entry -> entry.getValue() == null || entry.getValue().isEmpty())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            highlightInteractiveOfferCards(freeSlot);
        } else {
            highlightInteractiveOfferCards(null);
        }

        if(state.getCurrentPhase().equals("PLAYER_OFFER")){
            List <String> interactiveCardIds = new ArrayList<>();
            interactiveCardIds.addAll(state.getUpperRowCardIds());
            interactiveCardIds.addAll(state.getLowerRowCardIds());
            interactiveCardIds = interactiveCardIds.stream()
                    .filter(cardId -> !cardId.startsWith("EV"))
                    .collect(Collectors.toList());
            interactiveCardIds.addAll(state.getBuildingUpperIds());
            interactiveCardIds.addAll(state.getBuildingLowerIds());
            highlightInteractiveCards(interactiveCardIds);
        }
    }

    /**
     * Updates the totem icons displayed on the offer turn card.
     * During the PLACE_TOTEM phase totems are removed from the top,
     * while during PLAYER_OFFER they are added from the bottom.
     * Each totem is positioned using precomputed slot fractions based on player count.
     * @param state the current client game state
     */
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

    /**
     * Updates the totem icons displayed on each offer card track slot.
     * Each slot that has a player assigned shows that player's totem image,
     * scaled responsively within the slot container.
     * @param state the current client game state
     */
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
                        // --- BINDING ---
                        // Scale the totem to 30% of the slot height
                        totemImg.fitHeightProperty().bind(slot.heightProperty().multiply(0.30));

                        // No vertical translation needed (bound to 0.0)
                        totemImg.translateYProperty().bind(slot.heightProperty().multiply(0.0));

                        // Add the totem to the slot
                        slot.getChildren().add(totemImg);
                    }
                }
            }
        }
    }

    /**
     * Handles a click on an offer track slot during the PLACE_TOTEM phase.
     * Sends the {@code placeTotem} command to the server with the chosen slot letter.
     * @param letter the letter of the offer track slot that was clicked
     */
    private void handleOfferTrackClick(char letter) {
        try {
            this.server.placeTotem(this.myNickname, letter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles a click on a card during the PLAYER_OFFER phase.
     * Sends the {@code takeCard} command to the server with the clicked card ID.
     * @param cardId the identifier of the card that was clicked
     */
    private void handleCardClick(String cardId) {
        if (server != null) {
            try { server.takeCard(myNickname, cardId); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    /**
     * Updates the deck image to reflect the current game era.
     * The era number determines which era-specific deck background is shown.
     * @param era the current era index
     */
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
            System.err.println("Deck image not found for Era: " + era + ". Path looked for: " + deckPath);
        }
    }

    // =============================================== IMAGE RENDERING =====================================================

    /**
     * Creates an {@link ImageView} for a player's totem with appropriate visual effects.
     * The local player's totem receives an additional yellow glow to distinguish it.
     * @param nickname the player whose totem to render
     * @param state the current client game state (used to look up the player's totem color)
     * @return an ImageView displaying the totem with shadow effects applied
     */
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

    /**
     * Renders a list of card images into a given HBox row container.
     * Each card is wrapped in a {@link StackPane} for layering and stored in
     * {@link #renderedOfferCards} for later interactivity.
     * @param rowContainer the HBox row to populate with card images
     * @param cardIds the list of card identifiers to render
     */
    private void renderCards(HBox rowContainer, List<String> cardIds) {
        rowContainer.getChildren().clear();

        for (String cardId : cardIds) {
            Image cardImage = imageCache.renderCards(cardId);

            if (cardImage != null) {
                StackPane cardContainer = new StackPane();
                cardContainer.setAlignment(Pos.CENTER);

                ImageView imgView = new ImageView(cardImage);
                imgView.fitHeightProperty().bind(rowContainer.heightProperty().multiply(0.8));
                imgView.setPreserveRatio(true);

                // Add both to the StackPane
                cardContainer.getChildren().add(imgView);
                rowContainer.getChildren().add(cardContainer);
                renderedOfferCards.put(cardId, cardContainer);
            }
        }
    }

    // =============================================== ENLIGHTEN IMAGES =====================================================

    /**
     * Applies highlight and click interactivity to cards during the PLAYER_OFFER phase.
     * Interactive cards show a golden glow on hover and respond to clicks via {@link #handleCardClick}.
     * Non-interactive cards have all effects and handlers removed.
     * @param interactiveCardIds the list of card IDs that should be clickable, or null to clear all
     */
    private void highlightInteractiveCards(List<String> interactiveCardIds) {

        for (Map.Entry<String, StackPane> entry : renderedOfferCards.entrySet()) {
            String cardId = entry.getKey();
            StackPane container = entry.getValue();

            ImageView imgView = (ImageView) container.getChildren().get(0);

            if (interactiveCardIds != null && interactiveCardIds.contains(cardId)) {

                container.setCursor(Cursor.HAND);
                container.setOnMouseEntered(e -> {
                    imgView.setStyle("-fx-effect: dropshadow(gaussian, #ffd700, 15, 0.6, 0, 0);");
                });
                container.setOnMouseExited(e -> {
                    imgView.setStyle("");
                });
                container.setOnMouseClicked(e -> handleCardClick(cardId));

            } else {
                container.setCursor(Cursor.DEFAULT);
                container.setOnMouseEntered(null);
                container.setOnMouseExited(null);
                container.setOnMouseClicked(null);
                imgView.setStyle("");
            }
        }
    }

    /**
     * Applies highlight and click interactivity to offer track slots during the PLACE_TOTEM phase.
     * Free slots show a cyan glow on hover and respond to clicks via {@link #handleOfferTrackClick}.
     * Occupied or non-interactive slots have all effects and handlers cleared.
     * @param interactiveSlots the list of slot letters that should be clickable, or null to clear all
     */
    private void highlightInteractiveOfferCards(List<Character> interactiveSlots) {

        for (Map.Entry<Character, VBox> entry : renderedOfferSlots.entrySet()) {
            Character letter = entry.getKey();
            VBox slot = entry.getValue();

            if (interactiveSlots != null && interactiveSlots.contains(letter)) {

                slot.setCursor(Cursor.HAND);
                slot.setOnMouseEntered(e -> {
                    slot.setStyle("-fx-effect: dropshadow(gaussian, #00ffff, 15, 0.6, 0, 0);");
                });
                slot.setOnMouseExited(e -> {
                    slot.setStyle("");
                });
                slot.setOnMouseClicked(e -> handleOfferTrackClick(letter));

            } else {
                slot.setCursor(Cursor.DEFAULT);
                slot.setOnMouseEntered(null);
                slot.setOnMouseExited(null);
                slot.setOnMouseClicked(null);
                slot.setStyle("");
            }
        }
    }
}
