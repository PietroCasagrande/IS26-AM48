package it.polimi.ingsw.am48.network.server;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualView;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RmiViewAdapter implements VirtualView {

    private final VirtualViewRmi rmiCallback;
    // Rendo RmiViewAdapter asincrono con un Executor
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Runnable interface describes a class whose instances can be run as thread
    private final Runnable onDisconnect;

    // AtomicBoolean gives us a boolean value that can be updated atomically.
    // It's needed because the executor is asynchronous
    private final AtomicBoolean disconnected = new AtomicBoolean(false);

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final int HEARTBEAT_INTERNAL_SECONDS = 3;

    /*
    * Ogni notifica viene messa in coda su un thread dedicato a quel client.
    * Il broadcast ritorna immediatamente senza aspettare che il client riceva.
    * Il SingleThreadExecutor garantisce anche che i delta arrivino al client nello stesso ordine in cui son stati inviati.
    */

    public RmiViewAdapter(VirtualViewRmi rmiCallback, Runnable onDisconnect) {
        this.rmiCallback = rmiCallback;
        this.onDisconnect = onDisconnect;
        startHeartbeat();
    }

    public void startHeartbeat(){
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                rmiCallback.ping();
            } catch (RemoteException e) {
                System.err.println("RMI heartbeat failed, client disconnected.");
                handleDisconnect();
            }
        }, HEARTBEAT_INTERNAL_SECONDS, HEARTBEAT_INTERNAL_SECONDS, TimeUnit.SECONDS);
    }

    private void handleDisconnect() {
        // compareAndSet guarantees that onDisconnect is called only once even though more callbacks fail in succession
        if(disconnected.compareAndSet(false, true)) {
            heartbeatExecutor.shutdownNow();
            onDisconnect.run();
        }
    }

    @Override
    public void showGameDelta(GameDelta delta) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.showGameDelta(delta); }
            catch (RemoteException e) {
                System.err.println("RMI client disconnected: " + e.getMessage());
                // handle disconnect
                handleDisconnect();
            }
        });
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.showInitialSnapshot(snapshot); }
            catch (RemoteException e) {
                System.err.println("RMI client disconnected: " + e.getMessage());
                // handle disconnect
                handleDisconnect();
            }
        });
    }

    @Override
    public void reportError(String errorMessage) throws Exception {
        executor.submit(() -> {
            try { rmiCallback.reportError(errorMessage); }
            catch (RemoteException e) {
                System.err.println("RMI callback failed: " + e.getMessage());
                // handle disconnect
                handleDisconnect();
            }
        });
    }
}
