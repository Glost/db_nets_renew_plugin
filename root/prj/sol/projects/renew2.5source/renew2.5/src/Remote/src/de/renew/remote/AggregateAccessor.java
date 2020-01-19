package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.NoSuchElementException;


/**
 * An aggregate accessor can be wrapped around a
 * {@link de.renew.unify.Aggregate} token, allowing the
 * inspection of it's contents.
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * </p>
 *
 * AggregateAccessor.java
 * Created: Thu Jul 24  2003
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface AggregateAccessor extends ObjectAccessor {

    /**
     * Returns an enumeration of the aggregate's elements. If the
     * aggregate is a List, this enumeration will also support to
     * get the open tail of the list.
     *
     * @return   an enumeration of the aggregate's elements.
     *
     * @exception RemoteException
     *   if an RMI failure occurred.
     */
    public AggregateEnumerationAccessor elements() throws RemoteException;

    /**
     * An <code>AggregateEnumerationAccessor</code> can be
     * wrapped around the enumeration of elements of an
     * {@link de.renew.unify.Aggregate} object. This accessor
     * behaves like any {@link java.util.Enumeration}, returning
     * the elements one by one. Additionally, it allows to query
     * for the open tail of a list, if the underlying aggregate
     * is a {@link de.renew.unify.List}.
     **/
    public interface AggregateEnumerationAccessor extends Remote {

        /**
         * Tells whether the enumeration can return more elements
         * via <code>nextElement()</code>.
         *
         * @return   <code>true</code>, if there are objects left
         *           in this enumeration.
         *
         * @exception RemoteException
         *   if an RMI failure occurred.
         *
         * @see java.util.Enumeration#hasMoreElements
         **/
        public boolean hasMoreElements() throws RemoteException;

        /**
         * Returns the next element of the enumeration.
         *
         * @return  the next object in the enumeration, wrapped
         *          in an <code>ObjectAccessor</code>.
         *
         * @exception NoSuchElementException
         *   if there aren't any more elements in the enumeration.
         *
         * @exception RemoteException
         *   if an RMI failure occurred.
         *
         * @see java.util.Enumeration#nextElement
         **/
        public ObjectAccessor nextElement() throws RemoteException;

        /**
         * Returns the open tail of a list element enumeration.
         * This method may only be called when the underlying
         * aggregate of this enumeration accessor is an instance
         * of {@link de.renew.unify.List} and all other elements
         * of this enumeration have already been queried.
         *
         * @return  the open tail node of the list, wrapped in an
         *          <code>ObjectAccessor</code>.
         *
         * @exception UnsupportedOperationException
         *   if the aggregate of this enumeration is not a
         *   <code>List</code>.
         *
         * @exception IllegalStateException
         *   if the enumeration has not reached the end of the
         *   list yet.
         *
         * @exception RemoteException
         *   if an RMI failure occurred.
         *
         * @see de.renew.unify.ListIterator#getOpenTail
         **/
        public ObjectAccessor getOpenTail() throws RemoteException;
    }
}