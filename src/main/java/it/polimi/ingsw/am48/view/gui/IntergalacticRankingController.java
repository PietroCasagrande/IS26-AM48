package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for the intergalactic ranking (historical leaderboard) scene.
 * Displays a ranked list of past game results loaded from the server's database.
 */
public class IntergalacticRankingController {

    @FXML private VBox rankingList;

    private VirtualServer server;
    private ClientModel model;

    /**
     * Injects the server reference for future scene transitions.
     * @param server the virtual server reference
     */
    public void setServer(VirtualServer server) {
        this.server = server;
    }

    /**
     * Injects the model reference for future scene transitions.
     * @param model the client model reference
     */
    public void setModel(ClientModel model) {
        this.model = model;
    }

    /**
     * Populates the ranking list with historical game data.
     * Each entry shows rank number, player nickname, final score, and game date.
     * @param data a list of past game results to display
     */
    public void setData(List<GameResult> data) {
        rankingList.getChildren().clear();

        if (data == null || data.isEmpty()) {
            Label empty = new Label("No historical rankings available yet.");
            empty.getStyleClass().add("rk-empty");
            rankingList.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            GameResult r = data.get(i);
            HBox row = createRow(i + 1, r);
            rankingList.getChildren().add(row);
        }
    }

    /**
     * Creates a single row HBox for the leaderboard displaying rank, nickname, score, and date.
     * @param rank the ranking position (1-based)
     * @param r the game result data for this row
     * @return a styled HBox row
     */
    private HBox createRow(int rank, GameResult r) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 25, 10, 25));
        row.setPrefWidth(800);
        row.getStyleClass().add("rk-row");

        Label rankLabel = new Label("#" + rank);
        rankLabel.getStyleClass().add("rk-rank");

        Label nickLabel = new Label(r.getPlayerNickname());
        nickLabel.getStyleClass().add("rk-nick");

        Label scoreLabel = new Label(r.getFinalScore() + " PP");
        scoreLabel.getStyleClass().add("rk-score");

        Label dateLabel = new Label(r.getGameDate().toLocalDate().toString());
        dateLabel.getStyleClass().add("rk-date");

        row.getChildren().addAll(rankLabel, nickLabel, scoreLabel, dateLabel);
        return row;
    }

    /**
     * Navigates back to the game leaderboard scene.
     */
    @FXML
    private void handleBack() {
        Object ctrl = SceneManager.changeScene("leaderboard.fxml");
        if (ctrl instanceof LeaderboardController lc) {
            lc.setServer(server);
            lc.setModel(model);
        }
    }
}
