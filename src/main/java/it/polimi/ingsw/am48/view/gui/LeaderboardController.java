package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class LeaderboardController implements ModelObserver {

    @FXML private StackPane rootLeaderboard;
    @FXML private Label titleLabel;
    @FXML private Label winnerLabel;
    @FXML private VBox playersList;
    @FXML private Button buttonHistory;
    @FXML private Button buttonNewGame;
    @FXML private Button buttonMenu;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;

    private static final String[] totemColors = {"blue", "red", "black", "white", "yellow"};

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
        this.model.registerObserver(this);
        if (this.model.getState() != null) {
            onStateUpdated(this.model.getState());
        }
    }

    @FXML
    public void initialize() {
        myNickname = SceneManager.getNickname();
    }

    @Override
    public void onStateUpdated(ClientGameState state) {
        Platform.runLater(() -> buildLeaderboard(state));
    }

    @Override
    public void onError(String message) {
        System.err.println("Error: " + message);
    }

    private void buildLeaderboard(ClientGameState state) {
        myNickname = SceneManager.getNickname();
        String winner = state.getWinnerNickname();

        if (winner != null) {
            winnerLabel.setText(winner);
        }

        List<ClientPlayerState> sorted = state.getPlayers().values().stream()
                .sorted(Comparator.comparingInt(ClientPlayerState::getPoints).reversed())
                .toList();

        playersList.getChildren().clear();

        for (int i = 0; i < sorted.size(); i++) {
            ClientPlayerState player = sorted.get(i);
            HBox row = createPlayerRow(i + 1, player, state, winner);
            playersList.getChildren().add(row);
        }
    }

    private HBox createPlayerRow(int rank, ClientPlayerState player, ClientGameState state, String winner) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 25, 10, 25));
        row.setPrefWidth(800);

        boolean isMe = player.getNickname().equals(myNickname);
        boolean isWinner = player.getNickname().equals(winner);

        String style = "-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;";
        if (isWinner) {
            style = "-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 10; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 10;";
        } else if (isMe) {
            style += " -fx-background-color: rgba(255,215,0,0.12);";
        }
        row.setStyle(style);

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 22; -fx-font-weight: bold; -fx-min-width: 55;");

        ImageView avatar = new ImageView();
        String totemPath = getTotemPath(player.getNickname(), state);
        if (!totemPath.isEmpty()) {
            try {
                avatar.setImage(new Image(getClass().getResourceAsStream(totemPath)));
            } catch (Exception e) {
                System.err.println("Impossible loading the totem for " + player.getNickname());
            }
        }
        avatar.setFitHeight(55);
        avatar.setPreserveRatio(true);

        String displayName = player.getNickname();
        if (isMe) displayName += " (you)";
        Label nickLabel = new Label(displayName);
        String nickStyle = "-fx-text-fill: #FFF3E0; -fx-font-size: 20; -fx-font-weight: bold; -fx-min-width: 200;";
        if (isWinner) nickStyle += " -fx-text-fill: #FFD700;";
        nickLabel.setStyle(nickStyle);

        Label pointsLabel = new Label(player.getPoints() + " PP");
        pointsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20; -fx-font-weight: bold; -fx-min-width: 80;");

        Label foodLabel = new Label("Food: " + player.getFood());
        foodLabel.setStyle("-fx-text-fill: #FFF3E0; -fx-font-size: 18;");

        row.getChildren().addAll(rankLabel, avatar, nickLabel, pointsLabel, foodLabel);
        return row;
    }

    private String getTotemPath(String nickname, ClientGameState state) {
        List<String> entryOrder = state.getPlayers().values().stream()
                .map(ClientPlayerState::getNickname)
                .toList();

        int index = entryOrder.indexOf(nickname);
        if (index < 0 || index >= totemColors.length) return "";

        String color = totemColors[index];
        return "/it/polimi/ingsw/am48/view/gui/images/totems/" + color + "Totem.png";
    }

    @FXML
    private void handleNewGame() {
        try {
            // Close socket and end thread
            if (this.server instanceof SocketServerHandler) {
                ((SocketServerHandler) this.server).disconnect();
            }

            // Create brand-new model
            ClientModel newModel = new ClientModel();

            // Create brand-new socket
            SocketServerHandler newServer = new SocketServerHandler("localhost", 12345, newModel);

            // Restart thread
            Thread networkThread = new Thread(newServer);
            networkThread.setDaemon(true);
            networkThread.start();

            // Change to join game scene
            Object ctrl = SceneManager.changeScene("join-game-screen.fxml");
            if (ctrl instanceof JoinGameController jgc) {
                jgc.setServer(newServer);
                jgc.setModel(newModel);
            }

        } catch (IOException e) {
            System.err.println("Impossibile connettersi al server per la nuova partita!");
        }
    }

    @FXML
    private void handleMenu() {
        Object ctrl = SceneManager.changeScene("mesos-menu.fxml");
        if (ctrl instanceof MesosMenuController mmc) {
            mmc.setServer(server);
            mmc.setModel(model);
        }
    }

    @FXML
    private void handleHistory() {
        if (model == null || model.getState() == null) return;
        List<GameResult> data = model.getState().getLeaderboard();
        if (data == null || data.isEmpty()) return;

        Object ctrl = SceneManager.changeScene("intergalactic-ranking.fxml");
        if (ctrl instanceof IntergalacticRankingController irc) {
            irc.setServer(server);
            irc.setModel(model);
            irc.setData(data);
        }
    }
}
