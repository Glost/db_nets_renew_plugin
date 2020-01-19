package de.renew.navigator.diff;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
public interface FileDiffer {

    /**
     * Shows the diff of two files.
     *
     * @param f1 first file
     * @param f2 second file
     */
    void showFileDiff(File f1, File f2);
}