package de.renew.database;

import de.renew.net.NetInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;


/**
 * A net instance resolution input stream is a special
 * ObjectInputStream, that contains additional information
 * for resolving the net instance references (if any) of the
 * tokens back to the net instances.
 */
public class NetInstanceResolutionInputStream extends ObjectInputStream {

    /**
     * The net instance mapping hashtable.
     */
    private NetInstanceMap map;

    /**
     * Creates the net instance resolution input stream.
     * @param stream The InputStream to read object data from.
     * @param map The net instance mapping hashtable.
     */
    public NetInstanceResolutionInputStream(InputStream stream,
                                            NetInstanceMap map)
            throws IOException, StreamCorruptedException {
        super(stream);
        this.map = map;
    }

    /**
     * Returns a net instance for a given netID.
     * @param netID The net ID.
     * @return the corresponding net instance.
     */
    public NetInstance getNetInstanceByNetID(String netID) {
        return map.get(netID);
    }
}