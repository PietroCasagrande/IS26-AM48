package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class JoinGameController {
    @FXML
    private StackPane rootJoinGame;
    @FXML
    private VBox joinBox;
    @FXML
    private TextField nicknameTextField;
    @FXML
    private HBox numPlayersBox;
    @FXML
    private ToggleButton twoPlayersButton;
    @FXML
    private ToggleGroup numPlayersGroup;
    @FXML
    private ToggleButton threePlayersButton;
    @FXML
    private ToggleButton fourPlayersButton;
    @FXML
    private ToggleButton fivePlayersButton;
    @FXML
    private Button startGameButton;

    private VirtualServer server;
    private ClientModel model;

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        // Predefined 2 players button
        twoPlayersButton.setSelected(true);

        // Don't focus immediately on nicknameTextField
        Platform.runLater(() -> nicknameTextField.requestFocus());

        // Disable Start Game Button until player inserts their nickname
        startGameButton.disableProperty().bind(nicknameTextField.textProperty().isEmpty());

        // Maximum 15 characters for nickname
        nicknameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 15) {
                nicknameTextField.setText(oldValue); // Impedisce di scrivere più di 15 caratteri
            }
        });
    }

    @FXML
    private void handleStartGame() {
        ToggleButton selectedButton = (ToggleButton) numPlayersGroup.getSelectedToggle();
        if (selectedButton != null) {
            int numPlayers = (Integer) selectedButton.getUserData();
            String nickname = nicknameTextField.getText();

            // Debug
            System.out.println("Partenza per " + numPlayers + " giocatori. Nick: " + nickname);
            // Qui caricherai la scena successiva passandogli i dati

            // Calls joinGame method
            try{
                this.server.joinGame(numPlayers, nickname);
                LoadingLobbyController loadingLobby = (LoadingLobbyController) SceneManager.changeScene("loading-lobby.fxml");

                // Passiamo i riferimenti al nuovo controller
                if (loadingLobby != null) {
                    loadingLobby.setServer(server);
                    loadingLobby.setModel(model);
                }
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
                nicknameTextField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 3px;");

                // 2. Mostra un messaggio di errore (puoi usare una Label dedicata o un Alert)
                showErrorMessage("Il nickname '" + nickname + "' è già occupato. Scegline un altro!");

                // 3. Riporta il focus sulla textfield per far riscrivere subito l'utente
                nicknameTextField.requestFocus();
                nicknameTextField.selectAll();
            } catch (Exception e) {
                System.out.println("Generic error occurred.");
            }
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di Accesso");
        alert.setHeaderText(null); // Rimuove l'intestazione per renderlo più pulito
        alert.setContentText(message);
        alert.showAndWait(); // Blocca l'interfaccia finché l'utente non preme OK
    }
}
