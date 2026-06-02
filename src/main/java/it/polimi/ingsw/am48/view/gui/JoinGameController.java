package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller for the "Join Game" screen where the player enters their nickname,
 * selects the number of players, and starts a new game.
 * Implements {@link ModelObserver} to react to state changes (game started or lobby wait).
 */
public class JoinGameController implements ModelObserver {
    @FXML
    private VBox joinBox;
    @FXML
    private TextField nicknameTextField;
    @FXML
    private ToggleButton twoPlayersButton;
    @FXML
    private ToggleGroup numPlayersGroup;
    @FXML
    private Button startGameButton;

    private VirtualServer server;
    private ClientModel model;

    /**
     * Injects the server reference used to send join game requests.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference and registers this controller as an observer
     * to be notified when the game state transitions to the lobby or the game board.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
        this.model.registerObserver(this);
    }

    /**
     * Initializes the join game form with default values:
     * pre-selects 2 players, enforces a 15-character nickname limit,
     * and disables the start button until a nickname is entered.
     */
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
                nicknameTextField.setText(oldValue);
            }
        });
    }

    /**
     * Called when the game state changes. Transitions to the loading lobby if
     * waiting for players, or to the game board if the game has started.
     * Unregisters this observer when the game board is reached.
     * @param state the updated client game state
     */
     @Override
     public void onStateUpdated(ClientGameState state){
        Platform.runLater(() -> {
             if ("WAITING_FOR_PLAYERS".equals(state.getCurrentPhase())) {
                 LoadingLobbyController loadingLobby = (LoadingLobbyController) SceneManager.changeScene("loading-lobby.fxml");
                 if (loadingLobby != null) {
                     loadingLobby.setServer(server);
                     loadingLobby.setModel(model);
                 }
             }
             else {
                 GameBoardController gameBoardScene = (GameBoardController) SceneManager.changeScene("game-board.fxml");
                 if (gameBoardScene != null) {
                     gameBoardScene.setDependencies(SceneManager.getImageCache(), SceneManager.getSoundCache());
                     gameBoardScene.initialize(this.server, this.model);
                     model.unregisterObserver(this);
                 }
             }
         });
     }

    /**
     * Called when the model reports a login error.
     * Displays the error message in an alert, highlights the nickname field,
     * and plays the error sound effect.
     * @param message the error description
     */
    @Override
    public void onError(String message) {
        Platform.runLater(() -> {
            joinBox.setDisable(false);
            nicknameTextField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 3px;");

            // Show error message as an alert
            SceneManager.getSoundCache().playFAAAAH();
            showErrorMessage(message);

            // Focus on nickname textfield to rewrite user's nickname
            nicknameTextField.requestFocus();
            nicknameTextField.selectAll();
        });
    }

    /**
     * Handles the "Start Game" button click.
     * Reads the selected number of players and the entered nickname,
     * sends a {@code joinGame} request to the server, and saves the session.
     */
    @FXML
    private void handleStartGame() {
        ToggleButton selectedButton = (ToggleButton) numPlayersGroup.getSelectedToggle();
        if (selectedButton != null) {
            int numPlayers = (Integer) selectedButton.getUserData();
            String nickname = nicknameTextField.getText();

            // Calls joinGame method
            try {
                server.joinGame(numPlayers, nickname);
                joinBox.setDisable(true);
                SceneManager.setNickname(nickname);
                model.saveSession(nickname, numPlayers);
            } catch (Exception e) {
                System.out.println("Generic connection error occurred.");
                joinBox.setDisable(false);
            }
        }
    }

    /**
     * Handles the "Back to Menu" button click.
     * Unregisters this observer and returns to the main menu scene.
     */
    @FXML
    private void handleBackToMenu() {
        model.unregisterObserver(this);
        MesosMenuController menu = (MesosMenuController) SceneManager.changeScene("mesos-menu.fxml");
        if (menu != null) {
            menu.setServer(server);
            menu.setModel(model);
        }
    }

    /**
     * Displays an error alert dialog with the given message.
     * @param message the error message to show
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LOGIN ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
