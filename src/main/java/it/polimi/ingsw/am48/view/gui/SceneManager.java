package it.polimi.ingsw.am48.view.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage stage;
    private static Object server; // Riferimento al server
    private static Object model;  // Riferimento al model
    private static String nickname; // Nickname del giocatore locale

    public static void setup(Stage primaryStage, Object s, Object m) {
        stage = primaryStage;
        server = s;
        model = m;
    }

    public static void setNickname(String nick) {
        nickname = nick;
    }

    public static String getNickname() {
        return nickname;
    }

    public static Object changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Sostituisci il contenuto
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root, 1280, 720));
            } else {
                stage.getScene().setRoot(root);
            }

            stage.show();

            // RESTITUISCE IL CONTROLLER DELLA NUOVA SCENA
            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
