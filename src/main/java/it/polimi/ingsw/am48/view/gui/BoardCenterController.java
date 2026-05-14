package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.*;

public class BoardCenterController {

    //Root
    @FXML private VBox boardCenterRoot;

    // Contenitori Righe Carte
    @FXML private HBox upperCharacterRow;
    @FXML private HBox upperBuildingRow;
    @FXML private HBox lowerCharacterRow;
    @FXML private HBox lowerBuildingRow;

    // Contenitori Riga Centrale
    @FXML private Pane offerTurnCard;
    @FXML private HBox offerTrackContainer;
    @FXML private VBox deckContainer;

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
        updateCardRow(upperCharacterRow, state.getUpperRowCardIds());
        updateCardRow(upperBuildingRow, state.getBuildingUpperIds());
        updateCardRow(lowerCharacterRow, state.getLowerRowCardIds());
        updateCardRow(lowerBuildingRow, state.getBuildingLowerIds());

        updateOfferTrackTotems(state);
        // updateTurnOrderCard(state); // Qui aggiornerai la griglia di sinistra
    }

    // Aggiorna dinamicamente una qualsiasi HBox con le carte del momento
    private void updateCardRow(HBox rowContainer, List<String> cardIds) {
        rowContainer.getChildren().clear();

        for (String cardId : cardIds) {
            // Usa sempre la cache!
            Image cardImage = imageCache.renderImage(cardId);
            if (cardImage != null) {
                ImageView imgView = new ImageView(cardImage);
                imgView.setFitHeight(120);
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
            server.placeTotem(myNickname, letter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //TODO
    // Temporaneo per far compilare il codice
    private String getTotemPath(String nick, ClientGameState state) { return ""; }
}
