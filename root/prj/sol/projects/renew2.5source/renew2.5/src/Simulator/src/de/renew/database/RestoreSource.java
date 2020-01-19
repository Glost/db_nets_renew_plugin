package de.renew.database;

import de.renew.net.PlaceInstance;

import java.util.Hashtable;
import java.util.Vector;


/**
 * An object of this type may be used to extract the current information
 * about a system of nets out of a persistent store.
 *
 * While this information is read, it can be assumed that no changes
 * are performed to the database. On the other hand, it may be more efficient
 * to read the entire database into this object and use cached
 * data during the query.

 * This accessor interface suggests the following structure of the
 * database, but other structures are possible, too.
 *
 * table NET_INSTANCE: rows NET_INSTANCE_ID, NAME, DRAWING_OPEN
 * table TOKEN: rows TOKEN_ID, CLASS_NAME, SERIALISATION
 * table TOKEN_POSITION: rows TOKEN_ID, NET_INSTANCE_ID, PLACE_INSTANCE_ID, QUANTITY
 *
 * It is suggested that the type of the token is saved, too,
 * in order to be able to correctly interpret the
 * stored token object. It may be required to store
 * different token types in different tables, if the structure of the
 * tokens is very irregular.
 *
 * References to net instances should be saved as the net instance's ID
 * with an appropriate type. Upon restoring the net instances,
 * a map will be provided that allows to infer net instances from IDs.
 */
public interface RestoreSource {

    /**
     * Return a hashtable that maps the (globally unique) tokens
     * IDs to Java objects.
     *
     * @param map The net instance map to fetch a net instance by its id.
     *
     * @return the hashtable
     *
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public Hashtable<String, Object> getTokens(NetInstanceMap map)
            throws Exception;

    /**
     * Get all the tokens which exist at a place in a net.
     *
     * If the place contains references to net instances,
     * these have been typically saved by their net ID only.
     * A special lookup table simplifies the lookup of net instances.
     *
     * @see de.renew.net.NetInstance
     *
     * If the implementing class needs to access the
     * IDs that characterize this place instance, it is
     * suggested to use a code fragement like the following:
     * <code><pre>
     * String netID=placeInstance.getNetInstance().getID();
     * int placeID=placeInstance.getPlace().getID();
     * </pre></code>
     *
     * @param placeInstance The place instance to
     * look up the tokens of. Also contains the netInstance.
     * @param ids A vector, to which the implementing class must
     * append all IDs of token in this place
     *
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public void fillinAllTokens(PlaceInstance placeInstance, Vector<String> ids)
            throws Exception;

    /**
     * Get all net identifiers as strings.
     *
     * @return an array of all net IDs
     *
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public String[] getAllNetIDs() throws Exception;

    /**
     * Translates a net identifier into a net name.
     * The net name is typically the name given to the net file.
     *
     * @param netID net identifier
     * @return symbolic name associated with the identifier
     *
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public String getNetName(String netID) throws Exception;

    /**
     * Get all net identifiers as strings that
     * had a corresponding open drawing.
     *
     * @return an array of all such net IDs
     *
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public String[] getViewedNetIDs() throws Exception;

    /**
     * Returns if the simulation was inited (not terminated).
     * @return If the simulation was inited.
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public boolean wasSimulationInited() throws Exception;

    /**
     * Returns if the simulation was running.
     * @return If the simulation was running.
     * @exception Exception As an interface, RestoreSource
     * expects any exception to be thrown.
     */
    public boolean wasSimulationRunning() throws Exception;
}