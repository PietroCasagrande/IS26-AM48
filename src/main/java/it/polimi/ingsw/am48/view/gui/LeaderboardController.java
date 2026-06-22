package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the end-of-game leaderboard scene.
 * Displays the winner and a sorted list of all players with their ranks, totems, points, and food.
 * Provides buttons to start a new game, return to the main menu, or view historical rankings.
 * Implements {@link ModelObserver} to react to the final game state.
 */
public class LeaderboardController implements ModelObserver {

    @FXML private Label winnerLabel;
    @FXML private VBox playersList;

    private VirtualServer server;
    private ClientModel model;
    private String myNickname;

    private static final String[] totemColors = {"blue", "red", "black", "white", "yellow"};

    /**
     * Injects the server reference for starting new games or navigating.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference and registers as an observer.
     * If the state is already available, triggers an immediate leaderboard build.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
        this.model.registerObserver(this);
        if (this.model.getState() != null) {
            onStateUpdated(this.model.getState());
        }
    }

    /**
     * Initializes the controller by retrieving the local player's nickname.
     */
    @FXML
    public void initialize() {
        myNickname = SceneManager.getNickname();
    }

    /**
     * Called when the game state is updated. Builds or refreshes the leaderboard display.
     * @param state the current and final client game state
     */
    @Override
    public void onStateUpdated(ClientGameState state) {
        Platform.runLater(() -> buildLeaderboard(state));
    }

    /**
     * Called when the model reports an error. Logs the error to stderr.
     * @param message the error description
     */
    @Override
    public void onError(String message) {
        System.err.println("Error: " + message);
    }

    /**
     * Builds the leaderboard by sorting all players by points in descending order,
     * displaying the winner's name at the top, and creating a styled row for each player.
     * @param state the final game state containing all player information
     */
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

    /**
     * Creates a styled HBox row for a single player on the leaderboard.
     * Shows the rank, totem avatar, nickname, prestige points, and food count.
     * The winner's row and the local player's row receive distinct CSS styles.
     * @param rank the player's rank (1-based)
     * @param player the player's state
     * @param state the full game state (used to look up totem color)
     * @param winner the nickname of the winner
     * @return a styled HBox row
     */
    private HBox createPlayerRow(int rank, ClientPlayerState player, ClientGameState state, String winner) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 25, 10, 25));
        row.setPrefWidth(800);

        boolean isMe = player.getNickname().equals(myNickname);
        boolean isWinner = player.getNickname().equals(winner);

        row.getStyleClass().add("lb-row");
        if (isWinner) {
            row.getStyleClass().add("lb-row-winner");
        }

        Label rankLabel = new Label("#" + rank);
        rankLabel.getStyleClass().add("lb-rank");

        ImageView avatar = new ImageView();
        String totemPath = getTotemPath(player.getNickname(), state);
        if (!totemPath.isEmpty()) {
            try {
                avatar.setImage(new Image(getClass().getResourceAsStream(totemPath)));
            } catch (Exception e) {
                System.err.println("Unable to load totem for " + player.getNickname());
            }
        }
        avatar.setFitHeight(55);
        avatar.setPreserveRatio(true);

        String displayName = player.getNickname();
        if (isMe) displayName += " (you)";
        Label nickLabel = new Label(displayName);
        nickLabel.getStyleClass().add("lb-nick");
        if (isWinner) nickLabel.getStyleClass().add("lb-nick-winner");

        Label pointsLabel = new Label(player.getPoints() + " PP");
        pointsLabel.getStyleClass().add("lb-points");

        Label foodLabel = new Label("Food: " + player.getFood());
        foodLabel.getStyleClass().add("lb-food");

        row.getChildren().addAll(rankLabel, avatar, nickLabel, pointsLabel, foodLabel);
        return row;
    }

    /**
     * Returns the resource path for a player's totem image based on their
     * entry order in the game state.
     * @param nickname the player's nickname
     * @param state the game state (used to determine player index)
     * @return the resource path to the totem image, or empty string if not found
     */
    private String getTotemPath(String nickname, ClientGameState state) {
        List<String> entryOrder = state.getPlayers().values().stream()
                .map(ClientPlayerState::getNickname)
                .toList();

        int index = entryOrder.indexOf(nickname);
        if (index < 0 || index >= totemColors.length) return "";

        String color = totemColors[index];
        return "/it/polimi/ingsw/am48/view/gui/images/totems/" + color + "Totem.png";
    }

    /**
     * Handles the "Menu" button click. Returns to the main menu scene.
     */
    @FXML
    private void handleMenu() {
        Object ctrl = SceneManager.changeScene("mesos-menu.fxml");
        if (ctrl instanceof MesosMenuController mmc) {
            mmc.setServer(server);
            mmc.setModel(model);
        }
    }

    /**
     * Handles the "History" button click.
     * Loads historical leaderboard data from the model and transitions to the
     * intergalactic ranking scene.
     */
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
