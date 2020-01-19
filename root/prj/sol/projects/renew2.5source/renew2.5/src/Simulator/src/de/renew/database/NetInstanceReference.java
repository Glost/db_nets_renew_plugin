package de.renew.database;

import de.renew.net.NetInstance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A net instance reference points to a net instance,
 * instead of containing it. This is required to serialize
 * tokens that contain net instances, so that the tokens
 * can be restored all again containing the same net instances,
 * if some of them contained the same ones.
 */
public class NetInstanceReference implements Serializable {

    /**
     * The net instance ID this reference points to.
     */
    private String netID;

    /**
     * The net instance this reference points to
     * (temporarily memorized to resolve the reference.
     */
    private NetInstance netInstance;

    /**
     * Creates the net instance reference.
     * @param netID The net ID of the net instance this
     * references points to.
     */
    public NetInstanceReference(String netID) {
        this.netID = netID;
    }

    /**
     * Returns a string representation of the reference.
     */
    public String toString() {
        return "[" + netID + "]";
    }

    /**
     * Query the ID of the net instance. By default the ID is a
     * simple number, but this is not guaranteed.
     * @return the current ID string
     */
    public String getID() {
        return netID;
    }

    /**
     * Deserialization method to read an object from an
     * ObjectInputStream.
     * @param stream The ObjectInputStream to read from.
     * @return The deserialized object.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        netInstance = ((NetInstanceResolutionInputStream) stream)
                          .getNetInstanceByNetID(getID());
    }

    /**
     * Deserialization method to replace the object
     * after it has been deserialized.
     * In this case, the NetInstanceReference is replaced
     * by the corresponding NetInstance.
     * @return The corresponding NetInstance
     * the reference points to.
     */
    private Object readResolve() {
        return netInstance;
    }

    /**
     * Serialization method to write an object to an
     * ObjectOutputStream.
     * In this case, the NetInstance is not serialized
     * (it is serialized as null), because the deserialization
     * look the right net instance up.
     * @param stream The ObjectOutputStream to write to.
     */
    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        NetInstance netInstance = this.netInstance;
        this.netInstance = null;
        stream.defaultWriteObject();
        this.netInstance = netInstance;
    }
}