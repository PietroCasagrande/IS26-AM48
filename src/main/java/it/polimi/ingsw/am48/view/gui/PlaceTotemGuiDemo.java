package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;

public class PlaceTotemGuiDemo extends Application {

    private static int numPlayers = 3;
    private static final String[] NICKNAMES = {"Alice", "Bob", "Charlie", "Dave", "Eve"};

    @Override
    public void start(Stage stage) {
        GameManager gameManager = new GameManager();
        for (int i = 0; i < numPlayers; i++) {
            gameManager.joinGame(numPlayers, NICKNAMES[i]);
        }

        ClientModel clientModel = new ClientModel();
        Game game = gameManager.getGameByNickname(NICKNAMES[0]);
        clientModel.setInitialState(game.toSnapshot());

        VirtualServer mockServer = new VirtualServer() {
            @Override
            public void placeTotem(String nickname, char position) throws Exception {
                List<GameDelta> deltas = gameManager.placeTotem(nickname, position);
                for (GameDelta delta : deltas) {
                    clientModel.applyDelta(delta);
                }
                System.out.println("[DEMO] " + nickname + " placed on " + position
                        + " | Phase: " + clientModel.getState().getCurrentPhase()
                        + " | Order: " + clientModel.getState().getOfferTurnCardOrder()
                        + " | Track: " + clientModel.getState().getOfferTrackPositions());
            }

            @Override
            public void takeCard(String nickname, String cardId) {
                System.out.println("[DEMO] " + nickname + " clicked card " + cardId + " (no-op in demo)");
            }

            @Override
            public void joinGame(int numPlayers, String nickname) {
            }
        };

        SceneManager.setup(stage, mockServer, clientModel);
        SceneManager.setNickname(NICKNAMES[0]);

        Object ctrl = SceneManager.changeScene("game-board.fxml");
        if (ctrl instanceof GameBoardController gbc) {
            gbc.initialize(mockServer, clientModel);
        }

        stage.setTitle("Place Totem Demo - " + numPlayers + " players (you = " + NICKNAMES[0] + ")");
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();

        System.out.println("=== PLACE TOTEM DEMO ===");
        System.out.println("Players: " + numPlayers + " | You are: " + NICKNAMES[0]);
        System.out.println("Phase: " + clientModel.getState().getCurrentPhase());
        System.out.println("Turn order: " + clientModel.getState().getOfferTurnCardOrder());
        System.out.println("--> Click your totem on the left, then an offer card!");
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].matches("[2-5]")) {
            numPlayers = Integer.parseInt(args[0]);
        }
        launch(args);
    }
}
