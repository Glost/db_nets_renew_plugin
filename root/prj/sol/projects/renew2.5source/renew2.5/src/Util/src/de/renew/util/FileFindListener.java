package de.renew.util;

import java.util.Vector;


/**
 * This interface needs to be implemented by classes that want to be notified when
 * a file search has been finished.
 * @author Benjamin Schleinzer
 *
 */
public interface FileFindListener {

    /**
     * This method will be called after finishing a search. It will pass a list of the found files.
     * @param results
     */
    public void fileSearchDone(Vector<Object> results);
}