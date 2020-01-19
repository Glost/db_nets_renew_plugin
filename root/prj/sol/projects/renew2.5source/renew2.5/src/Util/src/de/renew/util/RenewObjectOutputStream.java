package de.renew.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Vector;


/** <p>
 * Adds to the classic ObjectOutputStream used to serialize
 * objects the feature to delay the serialization of particular
 * objects.
 * The intention of this feature is to cut down the recursion
 * depth on serialization, as delayed objects (and the part of
 * the serialization tree rooted in their fields) are written
 * from the root level of serialization.
 * </p>
 * <p>
 * The object containing the fields which should be delayed,
 * must declare these fields transient and implement the method:
 * <pre>
 * private void writeObject(ObjectOutputStream out) throws IOException {
 *     out.defaultWriteObject();
 *     ((RenewObjectOutputStream)out).delayedWriteObject(aField, this);
 * }
 * </pre>
 * The delayed written object will not be put into the stream
 * immediately, but remembered until a call to the method
 * <code>writeDelayedObjects()</code> or <code>close()</code>
 * appends all remembered objects to the stream.
 * The second parameter to the <code>delayedWriteObject</code>
 * method (<code>this</code>) is needed to allow to reassign the
 * written object to the object containing the field on
 * deserialization.
 * </p>
 * <p>
 * The stream produced by this class should be read by an instance
 * of <code>RenewObjectInputStream</code>. Be sure to read the
 * documentation of that class, too.
 * </p>
 * <p>
 * <b>Additional feature:</b>
 * </p>
 * <p>
 * Objects serialized by a <code>RenewObjectOutputStream</code>
 * instance can document the serialization progress by using the
 * methods <code>beginDomain()</code> and <code>endDomain()</code>
 * in their private <code>writeObject()</code> methods.
 * If the call to <code>endDomain()</code> is omitted when an
 * exception is raised, you obtain a protocol much like the
 * stack trace generated with exceptions, but containing objects
 * instead of class names.
 * The stream already uses this feature when delayed fields are
 * written: the delayed field owner is automatically documented
 * within the protocol.
 * </p>
 *
 *
 * RenewObjectOutputStream.java
 *
 * Created: Tue Feb  8  2000
 *
 * @see RenewObjectInputStream
 * @see DelayedFieldOwner
 *
 * @author Michael Duvigneau
 */
public class RenewObjectOutputStream extends ObjectOutputStream {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(RenewObjectOutputStream.class);
    protected final static String SECTIONLABEL = "Delayed:";
    private Vector<DelayedField> delayedObjects;
    private java.util.Stack<Object> objectTrace = new java.util.Stack<Object>();

    public RenewObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        delayedObjects = new Vector<DelayedField>();
    }

    public void delayedWriteObject(Object obj, DelayedFieldOwner owner) {
        // logger.debug("Delayed: "+obj);
        delayedObjects.addElement(new DelayedField(obj, owner));
    }

    public void writeDelayedObjects() throws IOException {
        int objectCount;
        Enumeration<DelayedField> objects;
        Object obj;


        // logger.debug("Writing delayed objects: START");
        // Start the delayed objects section by writing
        // the section label
        beginDomain(SECTIONLABEL);
        super.writeObject(SECTIONLABEL);

        while (!delayedObjects.isEmpty()) {
            // Get Size and Enumeration of delayedObjects and
            // ensure that adding objects while writing the
            // elements does not disturb the Enumeration.
            // The outer while loop ensures that objects
            // delayed while writing the elements are written
            // afterwards.
            objectCount = delayedObjects.size();
            objects = delayedObjects.elements();
            delayedObjects = new Vector<DelayedField>();


            // Write the number of elements to the Stream
            super.writeInt(objectCount);


            // Write all elements of the Enumeration to
            // the stream
            while (objects.hasMoreElements()) {
                obj = objects.nextElement();
                super.writeObject(obj);
            }
        }


        // Finish the delayed objects section by writing
        // an empty field group (e.g. the int 0).
        super.writeInt(0);

        endDomain(SECTIONLABEL);
        // logger.debug("Writing delayed objects: END");
    }

    public void close() throws IOException {
        try {
            // Write all remembered Objects not written now.
            // (If there are none, an empty field group 
            // will be written).
            writeDelayedObjects();
        } catch (IOException e) {
            throw new StreamCorruptedException("Exception occured while writing "
                                               + "delayed objects on close: "
                                               + e);
        } finally {
            // now, really close the stream
            super.close();
        }
    }

    public void beginDomain(Object owner) {
        objectTrace.push(owner);


        // logger.debug(("                                              "
        //                    .substring(0,objectTrace.size()))+"+"+owner);
    }

    public void endDomain(Object owner) {
        try {
            if (objectTrace.peek() == owner) {
                objectTrace.pop();
            } else {
                // ignore
                logger.error("End domain by wrong owner requested by " + owner);
                logger.error("Top of stack is " + objectTrace.peek());
                logger.error("Owner resides " + objectTrace.search(owner)
                             + " from top of stack");
            }
        } catch (EmptyStackException e) {
            // ignore
            logger.error("End domain on empty stack requested by " + owner);
        }


        // logger.debug(("                                              "
        //                    .substring(0,objectTrace.size()))+" -"+owner);
    }

    @SuppressWarnings("unchecked")
    public java.util.Stack<Object> getDomainTrace() {
        return (java.util.Stack<Object>) objectTrace.clone();
    }
}