package de.renew.refactoring.search.range;

import java.io.File;

import java.util.Iterator;


/**
 * Interface for file search ranges.
 *
 * @author 2mfriedr
 */
public interface FileSearchRange extends SearchRange {

    /**
     * Returns the number of files in the search range.
     *
     * @return the number of files
     */
    public int numberOfFiles();

    /**
     * Returns an iterator of all files in the search range.
     *
     * @return an iterator
     */
    public Iterator<File> files();
}