package it.polimi.ingsw.am48.view.gui;

import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.client.ClientModel;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.control.Label;

import java.io.IOException;

public class SplashScreenController {

    @FXML
    public StackPane root;
    @FXML
    public VBox textBox;
    @FXML
    public Label pressAnyButtonLabel;

    private FadeTransition pulse;

    private VirtualServerRmi server;
    private ClientModel model;

    public void setServer(VirtualServerRmi server) {
        this.server = server;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        // Questo metodo viene chiamato automaticamente da JavaFX appena carica l'FXML

        // 1. Facciamo lampeggiare la scritta
        pulse = new FadeTransition(Duration.seconds(1.2), pressAnyButtonLabel);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // 2. Ci mettiamo in ascolto del click del mouse
        root.setOnMouseClicked(event -> passaAlMenu());

        // 3. Ci mettiamo in ascolto della tastiera
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> passaAlMenu());
    }

    private void passaAlMenu() {
        // Fermiamo l'animazione per non sprecare memoria
        if (pulse != null) pulse.stop();

        System.out.println("Input ricevuto! Qui caricheremo il MenuIniziale.fxml");
        // Prossimamente qui inseriremo la chiamata al SceneManager

        try{
            // Carichiamo il file del menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mesos-menu.fxml"));
            Parent mainMenu = loader.load();

            // Prendiamo la scena attuale dal rootPane (lo StackPane della Splash)
            Scene scene = root.getScene();

            // Sostituiamo il contenuto della scena con il Menu
            scene.setRoot(mainMenu);
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
