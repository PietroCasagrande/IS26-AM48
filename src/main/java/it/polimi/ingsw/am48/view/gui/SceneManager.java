package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.view.CardDataRegistry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage stage;
    private static VirtualServer server; // Riferimento al server
    private static ClientModel model;  // Riferimento al model
    private static CardDataRegistry cardData;
    private static ImageCache imageCache;
    private static String nickname; // Nickname del giocatore locale

    public static void setup(Stage primaryStage, VirtualServer s, ClientModel m) {
        stage = primaryStage;
        server = s;
        model = m;
        cardData = new CardDataRegistry();
        imageCache = new ImageCache(cardData.getImagePaths());
    }

    public static void setNickname(String nick) {
        nickname = nick;
    }

    public static String getNickname() {
        return nickname;
    }

    public static ImageCache getImageCache() {return imageCache;}

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
