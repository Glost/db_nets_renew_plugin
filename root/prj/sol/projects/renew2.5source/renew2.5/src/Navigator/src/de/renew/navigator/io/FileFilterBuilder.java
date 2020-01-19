package de.renew.navigator.io;

import CH.ifa.draw.io.CombinationFileFilter;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-07
 */
public interface FileFilterBuilder {

    /**
     * Loads a file filter to use with IO operations.
     *
     * @return The file filter instance.
     */
    CombinationFileFilter buildFileFilter();

    /**
     * Finds out if a file should be opened externally.
     *
     * @param file the file to check
     * @return <code>true</code>, if the file should be opened externally
     */
    boolean isExternallyOpenedFile(File file);
}