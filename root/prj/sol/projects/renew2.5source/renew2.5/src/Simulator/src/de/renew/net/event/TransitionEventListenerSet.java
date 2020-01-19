package de.renew.net.event;



/**
 * A <code>TransitionEventListenerSet</code> manages a collection
 * of transition event listeners and is able to distribute events
 * to all of its listeners.
 *
 * Synchronization is done on this object.
 *
 * When an event is delivered synchronously, it is ensured that
 * the event set is locked, but that changes to the set of listeners
 * do not effect the current notifications.
 *
 * @author <a href="mailto:kummer@informatik.uni-hamburg.de">Olaf Kummer</a>
 */
public class TransitionEventListenerSet extends ListenerSet
        implements TransitionEventProducer {
    public synchronized void addTransitionEventListener(TransitionEventListener listener) {
        include(listener);
    }

    public synchronized void removeTransitionEventListener(TransitionEventListener listener) {
        exclude(listener);
    }

    /**
     * Informs all TransitionEventListeners that an instance
     * has just started firing.
     *
     * This method is called by all instances of this transition
     * if they have to send such an event.
     *
     * @see de.renew.net.TransitionInstance#firingStarted
     **/
    public synchronized void firingStarted(final FiringEvent fe) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((TransitionEventListener) listener).firingStarted(fe);
                }
            });
    }

    /**
     * Informs all TransitionEventListeners that an instance
     * has just completed firing.
     *
     * This method is called by all instances of this transition
     * if they have to send such an event.
     *
     * @see de.renew.net.TransitionInstance#firingComplete
     **/
    public synchronized void firingComplete(final FiringEvent fe) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((TransitionEventListener) listener).firingComplete(fe);
                }
            });
    }
}