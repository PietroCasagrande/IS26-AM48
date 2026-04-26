package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiViewAdapter implements VirtualView {

    private final VirtualViewRmi rmiCallback;
    // Rendo RmiViewAdapter asincrono con un Executor
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /*
    * Ogni notifica viene messa in coda su un thread dedicato a quel client.
    * Il broadcast ritorna immediatamente senza aspettare che il client riceva.
    * Il SingleThreadExecutor garantisce anche che i delta arrivino al client nello stesso ordine in cui son stati inviati.
    */

    public RmiViewAdapter(VirtualViewRmi rmiCallback) {
        this.rmiCallback = rmiCallback;
    }

    @Override
    public void showGameDelta(GameDelta delta) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.showGameDelta(delta); }
            catch (RemoteException e) {
                // handle disconnect
                System.err.println("RMI callback failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.showInitialSnapshot(snapshot); }
            catch (RemoteException e) {
                // handle disconnect
                System.err.println("RMI callback failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void reportError(String errorMessage) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.reportError(errorMessage); }
            catch (RemoteException e) {
                // handle disconnect
                System.err.println("RMI callback failed: " + e.getMessage());
            }
        });
    }
}
