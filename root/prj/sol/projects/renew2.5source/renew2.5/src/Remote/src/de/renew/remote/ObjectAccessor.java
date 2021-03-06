package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * An object accessor allows to view some attributes
 * of its associated object.
 * <p>
 * The associated object is not accessible directly but the
 * accessor provides String descriptions of the object as a
 * whole and its fields. This is somewhat like but less
 * powerful than what the java reflection api provides.
 * </p>
 * <p>
 * It is not allowed to create ObjectAccessor instances for
 * <code>null</code> references. Such references have to be passed
 * by value.
 * </p>
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * The intention is to keep the associated object on the
 * server and transmit only its description as strings.
 * </p>
 *
 * @author Olaf Kummer
 * @since Renew 1.6
 */
public interface ObjectAccessor extends Remote {

    /**
     * Returns a net instance accessor if the the object the accessor
     * encapsulates represents a {@link de.renew.net.NetInstance}.
     * Otherwise, a ClassCastException is thrown.
     * @return The net instance accessor.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     * @exception java.lang.ClassCastException if the object is no net instance.
     */
    public NetInstanceAccessor asNetInstance()
            throws RemoteException, ClassCastException;

    /**
     * Return the string representation of the accessed object as
     * generated by its <code>toString()</code> method.
     *
     * @return the object's string representation
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String asString() throws RemoteException;

    /**
     * Returns an aggregate accessor if the the object the accessor
     * encapsulates represents an {@link de.renew.unify.Aggregate}.
     * Otherwise, a ClassCastException is thrown.
     * @return the aggregate accessor.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     * @exception java.lang.ClassCastException if the object is no aggregate.
     */
    public AggregateAccessor asAggregate()
            throws RemoteException, ClassCastException;

    /**
     * Returns a text token accessor if the the object the accessor
     * encapsulates represents an {@link de.renew.util.TextToken}.
     * Otherwise, a ClassCastException is thrown.
     * @return the text token accessor.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     * @exception java.lang.ClassCastException if the object is no text token.
     */
    public TextTokenAccessor asTextToken()
            throws RemoteException, ClassCastException;

    /**
     * Return an object accessor for the value of the
     * field specified by the given index.
     * Returns <code>null</code> if the associated object of
     * this accessor is a primitive value or null reference.
     *
     * @param i the index of the field to be returned
     * @return an object accessor for the specified field's value
     * @exception java.lang.IllegalAccessException The field is not accessable
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public ObjectAccessor getField(int i)
            throws RemoteException, IllegalAccessException;

    /**
     * Return the number of public fields of the associated object.
     * Always zero for primitive values and null references.
     *
     * @return the number of public fields
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public int getFieldCount() throws RemoteException;

    /**
     * Return the names of all fields of the associated object.
     * Always an empty array for primitive values and null references.
     *
     * @return an array of field names
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String[] getFieldNames() throws RemoteException;

    /**
     * Return the values of all fields of the associated
     * object as a string representation.
     * This produces the same result as getting all fields one
     * by one via the <code>getField</code> method and putting
     * their <code>asString</code> results into an array.
     *
     * Returns always an empty array for primitive values and
     * null references.
     *
     * @return an array of field value string descriptions
     * @exception java.lang.IllegalAccessException The field is not accessable
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String[] getFieldValues()
            throws RemoteException, IllegalAccessException;


    /**
     * Returns whether the object the accessor encapsulates
     * is an instance of a given class.
     * @param testClass The class.
     * @return Whether the object represents such an instance.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean isInstanceOf(Class<?> testClass) throws RemoteException;
}