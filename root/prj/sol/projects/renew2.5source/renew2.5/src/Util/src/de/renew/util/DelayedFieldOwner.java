package de.renew.util;



/** <p>
 * This interface has to be implemented by objects containing
 * delayed written fields so they can be reassigned by a
 * RenewObjectInputStream.
 * </p>
 *
 * @see RenewObjectOutputStream
 * @see RenewObjectInputStream
 *
 * DelayedFieldOwner.java
 *
 * Created: Tue Feb  8  2000
 *
 * @author Michael Duvigneau
 */
public interface DelayedFieldOwner {

    /**
     * Used by <code>RenewObjectInputStream</code> to reassign a
     * field wich was delayed written to the
     * <code>RenewObjectOutputStream</code>.
     * Sorry, but there is <b>no information, which field</b>
     * has to be reassigned, the implementing class has to
     * guess it by the type of the given object.
     * It gets more simple if the class has only one delayed
     * field. :)
     * <p>
     * Objects using the delayed object writing support also
     * don't get informed when the last field was reassigned.
     **/
    public void reassignField(Object field) throws java.io.IOException;
}