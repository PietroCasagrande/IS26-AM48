
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
        // Inizializzazione dello SceneManager con i riferimenti necessari [cite: 2]
        SceneManager.setup(stage, server, model);

        // Se il model è null (test locale), viene creato un ClientModel di test
        if (model == null) {
            System.out.println("Attenzione: model null, inizializzo un ClientModel di test.");
            model = new ClientModel();
        }

        // Caricamento della scena della board di gioco [cite: 2]
        // Ritorna il controller associato al file FXML [cite: 2]
        Object controller = SceneManager.changeScene("game-board.fxml");

        if (controller instanceof GameBoardController gameBoardController) {
            // Iniezione delle dipendenze nel controller della board 
            gameBoardController.setServer(server);
            gameBoardController.setModel(model);
        } else {
            System.err.println("Errore: Il controller caricato non è GameBoardController!");
        }

        stage.setTitle("MESOS");

        // Gestione della chiusura pulita dell'applicazione
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }
}