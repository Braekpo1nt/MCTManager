package org.braekpo1nt.mctmanager.database;

import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is used for tests so that {@link java.util.concurrent.CompletableFuture}s which use
 * async threads in production can instead be run directly sequentially and immediately
 * on the main thread in tests. This reduces variability in tests and allows you
 * to test functionality instead of testing timing.<br>
 * This also toggles the {@link MyCustomServerMock#isBukkitOperationIsAllowed()} flag when running
 * fake async operations so that thread checks fail. See {@link #execute(Runnable)} for more details.
 */
public class ImmediateExecutorService extends AbstractExecutorService {
    
    /**
     * the server which contains {@link MyCustomServerMock#isBukkitOperationIsAllowed()} flag so
     * that we can toggle it to false, run an "async" operation that isn't truly async for tests,
     * then toggle it back to true for the successive operations.
     */
    private final MyCustomServerMock server;
    private volatile boolean shutdown;
    
    /**
     * @param server the server which contains {@link MyCustomServerMock#isBukkitOperationIsAllowed()}
     * flag so that we can toggle it to false, run an "async" operation that isn't truly async
     * for tests, then toggle it back to true for the successive operations.
     */
    public ImmediateExecutorService(@NotNull MyCustomServerMock server) {
        this.server = server;
    }
    
    @Override
    public void shutdown() {
        shutdown = true;
    }
    
    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return Collections.emptyList();
    }
    
    @Override
    public boolean isShutdown() {
        return shutdown;
    }
    
    @Override
    public boolean isTerminated() {
        return shutdown;
    }
    
    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return true;
    }
    
    /**
     * Sets {@link MyCustomServerMock#isBukkitOperationIsAllowed()} to {@code false},
     * runs the command,
     * then sets {@link MyCustomServerMock#setBukkitOperationIsAllowed(boolean)} to {@code true} again.<br>
     * This allows {@link MyCustomServerMock#isPrimaryThread()} to return {@code false} when
     * {@link org.mockbukkit.mockbukkit.AsyncCatcher#catchOp(String)} is run, thus throwing
     * an async exception when non-bukkit-safe operations are run.
     * @param command the runnable task
     */
    @Override
    public void execute(@NotNull Runnable command) {
        server.setBukkitOperationIsAllowed(false);
        command.run();
        server.setBukkitOperationIsAllowed(true);
    }
}
