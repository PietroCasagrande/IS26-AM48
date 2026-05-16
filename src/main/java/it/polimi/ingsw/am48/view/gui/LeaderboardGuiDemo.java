package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class LeaderboardGuiDemo extends Application {

    private static int numPlayers = 3;
    private static final String[] NICKNAMES = {"Simo", "Ale", "Pit", "Luke", "Eva"};

    @Override
    public void start(Stage stage) {
        GameManager gameManager = new GameManager();
        for (int i = 0; i < numPlayers; i++) {
            gameManager.joinGame(numPlayers, NICKNAMES[i]);
        }

        ClientModel clientModel = new ClientModel();
        Game game = gameManager.getGameByNickname(NICKNAMES[0]);
        clientModel.setInitialState(game.toSnapshot());

        clientModel.updatePlayerPoints("Simo", 45);
        clientModel.updatePlayerPoints("Ale", 38);
        clientModel.updatePlayerPoints("Pit", 31);
        if (numPlayers >= 4) clientModel.updatePlayerPoints("Luke", 27);
        if (numPlayers >= 5) clientModel.updatePlayerPoints("Eva", 1);
        clientModel.setWinnerNickname("Simo");

        VirtualServer mockServer = new VirtualServer() {
            @Override
            public void joinGame(int numPlayers, String nickname) {
            }

            @Override
            public void placeTotem(String nickname, char position) throws Exception {
            }

            @Override
            public void takeCard(String nickname, String cardId) {
            }
        };

        SceneManager.setup(stage, mockServer, clientModel);
        SceneManager.setNickname(NICKNAMES[3]);

        Object ctrl = SceneManager.changeScene("leaderboard.fxml");
        if (ctrl instanceof LeaderboardController lbc) {
            lbc.setServer(mockServer);
            lbc.setModel(clientModel);
        }

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].matches("[2-5]")) {
            numPlayers = Integer.parseInt(args[0]);
        }
        launch(args);
    }
}
