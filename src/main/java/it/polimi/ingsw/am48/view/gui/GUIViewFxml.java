package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIViewFxml extends Application {
    private static VirtualServerRmi server;
    private static ClientModel model;

    public static void setServer(VirtualServerRmi server) {
        GUIViewFxml.server = server;
    }

    public static void setModel(ClientModel model) {
        GUIViewFxml.model = model;
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("splash-screen.fxml"));
        Parent root = loader.load();

        SplashScreenController controller = loader.getController();
        controller.setServer(server);
        controller.setModel(model);

        stage.setScene(new Scene(root, 1280, 720));
        stage.setTitle("MESOS");
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}
