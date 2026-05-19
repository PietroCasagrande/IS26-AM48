package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.game.GameResult;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class IntergalacticRankingController {

    @FXML private StackPane rootRanking;
    @FXML private VBox rankingList;

    private VirtualServer server;
    private ClientModel model;

    public void setServer(VirtualServer server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

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

        Label playersLabel = new Label(r.getNumPlayers() + " players");
        playersLabel.getStyleClass().add("rk-players");

        Label dateLabel = new Label(r.getGameDate().toLocalDate().toString());
        dateLabel.getStyleClass().add("rk-date");

        Label gameIdLabel = new Label(r.getGameId());
        gameIdLabel.getStyleClass().add("rk-gameid");

        row.getChildren().addAll(rankLabel, nickLabel, scoreLabel, playersLabel, dateLabel, gameIdLabel);
        return row;
    }

    @FXML
    private void handleBack() {
        Object ctrl = SceneManager.changeScene("leaderboard.fxml");
        if (ctrl instanceof LeaderboardController lc) {
            lc.setServer(server);
            lc.setModel(model);
        }
    }
}
