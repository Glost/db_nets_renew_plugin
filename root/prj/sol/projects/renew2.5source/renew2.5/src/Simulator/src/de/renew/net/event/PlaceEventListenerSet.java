package de.renew.net.event;



/**
 * A <code>PlaceEventListenerSet</code> manages a collection
 * of place event listeners and is able to distribute events
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
public class PlaceEventListenerSet extends ListenerSet
        implements PlaceEventProducer {
    public synchronized void addPlaceEventListener(PlaceEventListener listener) {
        include(listener);
    }

    public synchronized void removePlaceEventListener(PlaceEventListener listener) {
        exclude(listener);
    }

    /**
     * Informs all PlaceEventListeners that an instance
     * has just changed its marking dramatically.
     *
     * This method is called by all instances of this place
     * if they have to send such an event.
     *
     * @param event the event to distribute
     *
     * @see PlaceEventListener#markingChanged
     **/
    public synchronized void markingChanged(final PlaceEvent event) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((PlaceEventListener) listener).markingChanged(event);
                }
            });
    }

    /**
     * Informs all PlaceEventListeners that a token
     * was just added to the marking of an instance.
     *
     * This method is called by all instances of this place
     * if they have to send such an event.
     *
     * @param event the event to distribute
     *
     * @see PlaceEventListener#tokenAdded
     **/
    public synchronized void tokenAdded(final TokenEvent event) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((PlaceEventListener) listener).tokenAdded(event);
                }
            });
    }

    /**
     * Informs all PlaceEventListeners that a token
     * was just removed from the marking of an instance.
     *
     * This method is called by all instances of this place
     * if they have to send such an event.
     *
     * @param event the event to distribute
     *
     * @see PlaceEventListener#tokenRemoved
     **/
    public synchronized void tokenRemoved(final TokenEvent event) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((PlaceEventListener) listener).tokenRemoved(event);
                }
            });
    }

    /**
     * Informs all PlaceEventListeners that a token
     * was just tested within the marking of an instance.
     *
     * This method is called by all instances of this place
     * if they have to send such an event.
     *
     * @param event the event to distribute
     *
     * @see PlaceEventListener#tokenTested
     **/
    public synchronized void tokenTested(final TokenEvent event) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((PlaceEventListener) listener).tokenTested(event);
                }
            });
    }

    /**
     * Informs all PlaceEventListeners that a token
     * was just untested within the marking of an instance.
     *
     * This method is called by all instances of this place
     * if they have to send such an event.
     *
     * @param event the event to distribute
     *
     * @see PlaceEventListener#tokenUntested
     **/
    public synchronized void tokenUntested(final TokenEvent event) {
        dispatch(new ListenerSetDispatcher() {
                public void dispatchTo(Object listener) {
                    ((PlaceEventListener) listener).tokenUntested(event);
                }
            });
    }
}