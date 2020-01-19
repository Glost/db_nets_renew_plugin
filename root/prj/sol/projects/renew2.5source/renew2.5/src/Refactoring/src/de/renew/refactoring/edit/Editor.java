package de.renew.refactoring.edit;

import java.util.Iterator;
import java.util.List;


/**
 * Interface for editors that perform edits similarly to an {@link Iterator}.
 * Results of type R are returned from every edit. Clients of this interface
 * typically collect the results into a list.
 *
 * @author 2mfriedr
 */
public interface Editor<R> {

    /**
     * Returns the number of edits to be performed.
     *
     * @return the number of edits
     */
    public int getNumberOfEdits();

    /**
     * Checks if there are more edits to perform.
     *
     * @return {@code true} if there are more edits to perform, otherwise
     * {@code false}
     */
    public boolean hasNextEdit();

    /**
     * Returns the progress of the operation.
     *
     * @return an integer that is greater than or equal to 0 and less than or
     * equal to 100
     */
    public int getProgress();

    /**
     * Returns a string representation of the current edit, e.g. the name of
     * the artifact that is currently being edited.
     *
     * @return a string representation
     */
    public String getCurrentEditString();

    /**
     * Performs the next edit.
     *
     * @return an object of type R
     */
    public R performNextEdit();

    /**
     * Synchronously performs the remaining edits and returns the results as a
     * list. After calling this method, {@link Editor#hasNextEdit()} will
     * return {@code false} and {@link Editor#getProgress()} will return 100.
     *
     * @return a list of objects of type R
     */
    public List<R> performAllEdits();
}