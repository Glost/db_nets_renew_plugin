package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.unify.Aggregate;
import de.renew.unify.ListIterator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Iterator;


/**
 * This class implements the <code>AggregateAccessor</code>
 * interface and nothing more.
 * <p>
 * </p>
 *
 * AggregateAccessorImpl.java
 * Created: Thu Jul 24  2003
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class AggregateAccessorImpl extends ObjectAccessorImpl
        implements AggregateAccessor {

    /**
     * The wrapped object, still typed as <code>Aggregate</code>.
     **/
    private Aggregate aggregate;

    /**
     * Creates a new accessor for the given aggregate.
     *
     * @param aggregate    the aggregate for the accessor.
     *
     * @param environment  the simulation environment where this
     *                     object belongs to.
     *
     * @exception RemoteException
     *   if a RMI failure occured.
     **/
    public AggregateAccessorImpl(Aggregate aggregate,
                                 SimulationEnvironment environment)
            throws RemoteException {
        super(aggregate, environment);
        this.aggregate = aggregate;
    }

    /**
     * @see AggregateAccessor#elements()
     **/
    public AggregateEnumerationAccessor elements() throws RemoteException {
        return new AggregateEnumerationAccessorImpl(aggregate.iterator());
    }

    /**
     * @see AggregateEnumerationAccessor
     **/
    private class AggregateEnumerationAccessorImpl extends UnicastRemoteObject
            implements AggregateEnumerationAccessor {
        private Iterator<Object> iterator;

        public AggregateEnumerationAccessorImpl(Iterator<Object> enumeration)
                throws RemoteException {
            super(0, SocketFactoryDeterminer.getInstance(),
                  SocketFactoryDeterminer.getInstance());
            this.iterator = enumeration;
        }

        /**
         * @see AggregateEnumerationAccessor#hasMoreElements()
         **/
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        /**
         * @see AggregateEnumerationAccessor#nextElement()
         **/
        public ObjectAccessor nextElement() throws RemoteException {
            return createObjectAccessor(iterator.next(), environment);
        }

        /**
         * @see AggregateEnumerationAccessor#getOpenTail()
         **/
        public ObjectAccessor getOpenTail() throws RemoteException {
            if (iterator instanceof ListIterator) {
                return createObjectAccessor(((ListIterator) iterator)
                           .getOpenTail(), environment);
            } else {
                throw new UnsupportedOperationException("Cannot get open tail on non-list aggregate.");
            }
        }
    }
}