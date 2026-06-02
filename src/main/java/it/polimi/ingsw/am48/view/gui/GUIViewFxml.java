package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main entry point for launching the JavaFX FXML-based graphical user interface.
 * This class extends {@link Application} and sets up the primary stage via {@link SceneManager},
 * then shows the splash screen as the initial scene.
 */
public class GUIViewFxml extends Application {
    private static VirtualServer server;
    private static ClientModel model;

    /**
     * Injects the server reference into the GUI application before the JavaFX runtime starts.
     * @param server the virtual server reference
     */
    public static void setServer(VirtualServer server) {
        GUIViewFxml.server = server;
    }

    /**
     * Injects the model reference into the GUI application before the JavaFX runtime starts.
     * @param model the client model reference
     */
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

        // Handle application close: exit the JavaFX platform and terminate the process
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}