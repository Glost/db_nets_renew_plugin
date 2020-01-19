package de.renew.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;


/** <p>
 * This stream is needed to read streams written by a
 * <code>RenewObjectOutputStream</code>.
 * As with all streams, make sure to read objects in
 * exactly the same order as they were written.
 * This does not apply to <code>delayedWriteObject(obj,
 * this)</code> calls in the <code>private writeObject()</code>
 * method of serializable classes. Delayed written objects
 * cannot be read from the stream on deserialization.
 * As a consequence: If your only reason to implement the
 * <code>private writeObject()</code> method was the usage
 * of the <code>RenewObjectOutputStream</code>, there is
 * no need to implement the corresponding <code>private
 * readObject()</code> method!
 * </p>
 * <p>
 * If you called <code>writeDelayedObjects()</code> on
 * the <code>RenewObjectOutputStream</code>, make also
 * sure you call <code>readDelayedObjects()</code> at
 * the correct place in your reading order.
 * This stream also tries to read delayed written objects
 * when <code>close()</code> is called, but only objects
 * written by <code>RenewObjectOutputStream.close()</code>
 * can be deserialized this way.
 * </p>
 * <p>
 * Deserialized fields (originally delayed written) are
 * immediately reassigned to the deserialized objects
 * which correspond to the objects which contained the
 * fields before deserialization.
 * In order to accomplish the reassignment, the containing
 * object's class must implement the interface
 * <code>DelayedFieldOwner</code>. Be sure to read the
 * interface's documentation, too.
 * </p>
 * <p>
 * When using the delayed object support on serialization,
 * pay attention to the fact, that on deserialization
 * objects probably are not completly restored after an
 * <code>readObject()</code> call, the delayed written
 * fields may be still missing.
 * The complete restauration of all objects can only be
 * guaranteed after closing the <code>RenewObjectInputStream</code>.
 * If an explicit call to <code>writeDelayedObjects()</code>
 * was made on serialization, the completeness of objects
 * written before that call can be assured after the
 * corresponding <code>readDelayedObjects()</code> call.
 * </p><p>
 * The delayed fields will be read using
 * <code>de.renew.util.ClassSource</code> to provide
 * its ability of reloading all user defined classes.
 * Sorry for the inconvenience, but it was not possible to
 * add this feature to the inherited <code>readObject()</code>
 * method, too.
 * </p>
 * <p>
 * <b>Additional feature:</b>
 * </p>
 * <p>
 * Via the parameter <code>copiousBehaviour</code>, it is possible to
 * select between unmodified restoration of objects (<code>false</code>)
 * and a modification that avoids name clashes within global registries
 * (<code>true</code>).
 * If <code>copiousBehaviour</code> is enabled, deserialized objects can
 * react to this property by querying {@link #isCopiousBehaviour}.  This
 * feature is of interest when the data stream does not comprise full state
 * information and the deserialized result should be mergeable into an
 * existing state.
 * </p>
 * <p>
 * <em> The <code>copiousBehaviour</code> attribute depends on cooperation
 * of the objects contained in the data stream to be deserialized.  Due to
 * this fact, it can not be guaranteed that the result is really a
 * conflict-free copy of objects. </em>
 * </p>
 * <p>
 * For the time being (2007/02/16), the following classes are known to
 * respect the <code>copiousBehaviour</code> attribute:
 * {@link de.renew.net.Net} (changes name),
 * {@link de.renew.net.NetInstanceImpl} (changes id),
 * {@link de.renew.net.TransitionInstance} (hooks into <code>SearchQueue</code>)
 * </p>
 *
 * RenewObjectInputStream.java
 * Created: Tue Feb  8  2000
 *
 * @author Michael Duvigneau
 * @see RenewObjectOutputStream
 * @see DelayedFieldOwner#reassignField
 * @see ClassSource
 **/
public class RenewObjectInputStream extends ObjectInputStream {
    private final boolean copiousBehaviour;

    /**
     * Creates a new <code>RenewObjectInputStream</code> instance with
     * default (non-copious) behaviour.  This is the same as calling
     * {@link #RenewObjectInputStream(InputStream, boolean) RenewObjectInputStream(in, false)}.
     *
     * @param in  the stream to be enhanced by
     *            <code>RenewObjectInputStream</code>'s special features.
     **/
    public RenewObjectInputStream(InputStream in)
            throws IOException, StreamCorruptedException {
        this(in, false);
    }

    /**
     * Creates a new <code>RenewObjectInputStream</code> instance.
     *
     * @param in  the stream to be enhanced by
     *            <code>RenewObjectInputStream</code>'s special features.
     * @param copiousBehaviour selects between unmodified restoration of
     *            objects (<code>false</code>) and a modification that
     *            avoids name clashes int global registries
     *            (<code>true</code>).
     * @see RenewObjectInputStream class comment
     **/
    public RenewObjectInputStream(InputStream in, boolean copiousBehaviour)
            throws IOException, StreamCorruptedException {
        super(in);
        this.copiousBehaviour = copiousBehaviour;
    }

    /**
     * Tells whether deserialized objects should modify themselves to avoid
     * name clashes in global registries.
     *
     * @return <code>true</code> if copious behaviour has been requested on
     *         stream creation.
     * @see RenewObjectInputStream class comment
     **/
    public boolean isCopiousBehaviour() {
        return copiousBehaviour;
    }

    public void readDelayedObjects() throws IOException, ClassNotFoundException {
        int fieldCount;
        int i;
        DelayedField field;


        // logger.debug("Reading delayed objects: START");
        // Data structure in the stream:
        // - SECTIONLABEL
        // - any number of field groups (containing at least one field each)
        // - last field group (containing 0 fields)
        //
        // A field group is preceeded by an integer representing
        // the number of fields in the group.
        // Check for the section label
        if (super.readObject().equals(RenewObjectOutputStream.SECTIONLABEL)) {
            // Get number of fields in the first group	    
            fieldCount = super.readInt();

            // While there are non-empty groups...
            while (fieldCount > 0) {
                // ... read exactly fieldCount fields ...
                for (i = 1; i <= fieldCount; i++) {
                    field = (DelayedField) ClassSource.readObject(this);
                    field.reassign();
                }


                // ... and get number of fields in the next group
                fieldCount = super.readInt();
            }
        } else {
            throw new StreamCorruptedException("Stream did not contain expected delayed object section label.");
        }

        // logger.debug("Reading delayed objects: END");
    }

    public void close() throws IOException {
        // Read delayed objects written on close() of the
        // RenewObjectOutputStream (there should be at
        // least one empty field group).
        // If an Exception occurs, close the stream anyway.
        // I don't want to rethrow it immediately, because
        // the caller perhaps does not expect the writing
        // of data when close() is called.
        try {
            readDelayedObjects();
        } catch (ClassNotFoundException e) {
            throw new StreamCorruptedException("Exception occured while reading "
                                               + "delayed objects on close: "
                                               + e);
        } catch (IOException e) {
            throw new StreamCorruptedException("Exception occured while reading "
                                               + "delayed objects on close: "
                                               + e);
        } finally {
            // Now, really close the stream.
            super.close();
        }
    }
}