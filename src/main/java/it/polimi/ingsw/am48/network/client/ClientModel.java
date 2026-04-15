package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

public class ClientModel {
    // questo metodo aggiorna il modello locale con i cambiamenti dal server
    public void applyDelta(GameDelta delta) {
        // qui va la logica per modificare le tue strutture dati locali
    }

    // imposta lo stato iniziale (lobby o partita)
    public void setInitialState(GameSnapshot snapshot) {
        // memorizza lo snapshot ricevuto dal server
    }

    // notifica errori alla view (es. popup o messaggi in console)
    public void notifyError(String message) {
        System.err.println("errore dal server: " + message);
    }
}