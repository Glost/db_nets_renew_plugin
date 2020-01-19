package de.renew.net.event;

import org.apache.log4j.Logger;

import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.SimulatorEventQueue;

import java.util.HashSet;
import java.util.Set;


/**
 * A <code>ListenerSet</code> manages a collection
 * of event listeners and is able to distribute events
 * to all of its listeners.
 * <p>
 * Synchronization is done on this object.
 * <p>
 * When an event is delivered synchronously, it is ensured that
 * the listener set is locked, so that attempted changes to the
 * set of listeners do not effect the current notifications.
 * <p>
 * This is a utility class that is not type safe. It should be
 * subclassed to get adequate functionality.
 *
 * @author <a href="mailto:kummer@informatik.uni-hamburg.de">Olaf Kummer</a>
 */
public class ListenerSet {
    public static final Logger logger = Logger.getLogger(ListenerSet.class);

    /**
     * The listeners to notify if events occur.
     **/
    private transient Set<NetEventListener> listeners = new HashSet<NetEventListener>();

    synchronized void include(NetEventListener listener) {
        listeners.add(listener);
    }

    synchronized void exclude(NetEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Call the dispatcher once for each registered listener.
     **/
    synchronized void dispatch(final ListenerSetDispatcher dispatcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        NetEventListener[] listenerArray = listeners.toArray(new NetEventListener[] {  });
        for (int x = 0; x < listenerArray.length; x++) {
            if (listenerArray[x].wantSynchronousNotification()) {
                try {
                    dispatcher.dispatchTo(listenerArray[x]);
                } catch (RuntimeException e) {
                    logger.error("Error while dispatching net event: " + e, e);
                } catch (Error e) {
                    logger.error("Error while dispatching net event: " + e, e);
                }
            } else {
                final NetEventListener listener = listenerArray[x];
                SimulatorEventQueue.enqueue(new Runnable() {
                        public void run() {
                            assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
                            try {
                                dispatcher.dispatchTo(listener);
                            } catch (RuntimeException e) {
                                logger.error("Error while dispatching net event: "
                                             + e, e);
                            } catch (Error e) {
                                logger.error("Error while dispatching net event: "
                                             + e, e);
                            }
                        }
                    });
            }
        }
    }
}