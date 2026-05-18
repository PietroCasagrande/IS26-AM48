package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ModelObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class JoinGameController implements ModelObserver {
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

    private boolean hasGameStarted = false;

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
        this.model.registerObserver(this);
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
                nicknameTextField.setText(oldValue);
            }
        });
    }

     @Override
     public void onStateUpdated(ClientGameState state){
        if(hasGameStarted) return;
        Platform.runLater(() -> {
             if ("WAITING_FOR_PLAYERS".equals(state.getCurrentPhase())) {
                 LoadingLobbyController loadingLobby = (LoadingLobbyController) SceneManager.changeScene("loading-lobby.fxml");
                 if (loadingLobby != null) {
                     loadingLobby.setServer(server);
                     loadingLobby.setModel(model);
                 }
             }
             else if("PLACE_TOTEM".equals(state.getCurrentPhase())) {
                 GameBoardController gameBoardScene = (GameBoardController) SceneManager.changeScene("game-board.fxml");
                 if (gameBoardScene != null) {
                     gameBoardScene.setDependencies(SceneManager.getImageCache());
                     gameBoardScene.initialize(this.server, this.model);
                     hasGameStarted = true;
                 }
             }
             else {
                 System.out.println("Generic error occurred. Cannot set in waiting for players state.");
             }
         });
     }

    @Override
    public void onError(String message) {
        if(hasGameStarted) return;
        Platform.runLater(() -> {
            joinBox.setDisable(false);
            nicknameTextField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 3px;");

            // Shows error message as an alert
            showErrorMessage(message);

            // Focus on nickname textfield to rewrite user's nickname
            nicknameTextField.requestFocus();
            nicknameTextField.selectAll();
        });
    }

    @FXML
    private void handleStartGame() {
        ToggleButton selectedButton = (ToggleButton) numPlayersGroup.getSelectedToggle();
        if (selectedButton != null) {
            int numPlayers = (Integer) selectedButton.getUserData();
            String nickname = nicknameTextField.getText();

            // Calls joinGame method
            try{
                server.joinGame(numPlayers, nickname);
                joinBox.setDisable(true);
                SceneManager.setNickname(nickname);
                model.saveSession(nickname, numPlayers);
            } catch (Exception e){
                System.out.println("Generic connection error occurred. Please try again.");
            }
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LOGIN ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
