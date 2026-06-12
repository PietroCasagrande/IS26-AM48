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

/**
 * Server-side adapter that wraps a single client's {@link VirtualViewRmi} remote callback
 * and exposes it through the technology-agnostic {@link VirtualView} interface.
 *
 * <p>This class has two responsibilities beyond simple delegation:
 * <ul>
 *   <li><b>Asynchronous, ordered delivery:</b> every {@code show*}/{@code reportError} call
 *       is submitted to a dedicated single-thread {@link ExecutorService}. The calling
 *       thread (typically {@link MesosServer}'s broadcast loop) returns immediately without
 *       waiting for the RMI call to complete, while the single-thread executor guarantees
 *       that notifications are delivered to this client in the same order they were
 *       submitted.</li>
 *   <li><b>Disconnection detection:</b> a {@link ScheduledExecutorService} pings the client
 *       every {@value #HEARTBEAT_INTERNAL_SECONDS} seconds via {@link VirtualViewRmi#ping}.
 *       A {@link RemoteException} from the heartbeat — or from any queued notification —
 *       is treated as a disconnection and triggers {@link #handleDisconnect()} exactly once,
 *       guarded by the {@link AtomicBoolean} {@code disconnected} flag.</li>
 * </ul>
 */
public class RmiViewAdapter implements VirtualView {

    private final VirtualViewRmi rmiCallback;
    // executor makes RmiViewAdapter asynchronous
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Runnable interface describes a class whose instances can be run as thread
    private final Runnable onDisconnect;

    // AtomicBoolean gives us a boolean value that can be updated atomically.
    // It's needed because the executor is asynchronous
    private final AtomicBoolean disconnected = new AtomicBoolean(false);

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final int HEARTBEAT_INTERNAL_SECONDS = 3;

    /**
     * Constructs the adapter around the given client callback and starts the heartbeat.
     *
     * @param rmiCallback  the client's remote callback object, as registered via
     *                      {@link it.polimi.ingsw.am48.network.VirtualServerRmi#connect}
     * @param onDisconnect the action to run when this client is detected as disconnected;
     *                      invoked at most once (see {@link #handleDisconnect()})
     */
    public RmiViewAdapter(VirtualViewRmi rmiCallback, Runnable onDisconnect) {
        this.rmiCallback = rmiCallback;
        this.onDisconnect = onDisconnect;
        startHeartbeat();
    }

    /**
     * Schedules a recurring task that calls {@link VirtualViewRmi#ping} on the client
     * every {@value #HEARTBEAT_INTERNAL_SECONDS} seconds.
     *
     * <p>If a ping throws {@link RemoteException}, the client is considered disconnected
     * and {@link #handleDisconnect()} is invoked.
     */
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

    /**
     * Handles a detected client disconnection, idempotently.
     *
     * <p>{@link AtomicBoolean#compareAndSet} ensures that even if multiple concurrent
     * callbacks (heartbeat and queued notifications) fail in quick succession, the
     * heartbeat executor is shut down and {@code onDisconnect} is run exactly once.
     */
    private void handleDisconnect() {
        // compareAndSet guarantees that onDisconnect is called only once even though more callbacks fail in succession
        if(disconnected.compareAndSet(false, true)) {
            heartbeatExecutor.shutdownNow();
            onDisconnect.run();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Submits the delivery of {@code delta} to the dedicated executor and returns
     * immediately. If the underlying remote call fails with {@link RemoteException},
     * the client is treated as disconnected via {@link #handleDisconnect()}.
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Submits the delivery of {@code snapshot} to the dedicated executor and returns
     * immediately. If the underlying remote call fails with {@link RemoteException},
     * the client is treated as disconnected via {@link #handleDisconnect()}.
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Submits the delivery of {@code errorMessage} to the dedicated executor and
     * returns immediately. If the underlying remote call fails with {@link RemoteException},
     * the client is treated as disconnected via {@link #handleDisconnect()}.
     */
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
