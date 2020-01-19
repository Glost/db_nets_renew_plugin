package de.renew.net.event;

public interface NetEventListener extends java.util.EventListener {

    /**
     * Announce whether notifications should be done
     * synchronously or asynchronously. Synchronous access
     * might cause deadlocks due to subsequent synchronization,
     * Asynchronous access might cause notifications to arrive
     * too late.
     *
     * @return <code>true</code>, if notifications should be done
     *         synchronously.
     */
    public boolean wantSynchronousNotification();
}