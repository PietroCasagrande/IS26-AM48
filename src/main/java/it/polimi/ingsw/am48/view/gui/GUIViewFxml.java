package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Classe principale per il lancio dell'interfaccia grafica FXML.
 */
public class GUIViewFxml extends Application {
    private static VirtualServer server;
    private static ClientModel model;

    public static void setServer(VirtualServer server) {
        GUIViewFxml.server = server;
    }

    public static void setModel(ClientModel model) {
        GUIViewFxml.model = model;
    }

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setup(stage, server, model);

        Object controller = SceneManager.changeScene("splash-screen.fxml");
        if (controller instanceof SplashScreenController splashScreenController) {
            splashScreenController.setServer(server);
            splashScreenController.setModel(model);
        }

        stage.setTitle("MESOS");

        // Gestione della chiusura dell'applicazione
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}