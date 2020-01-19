package de.renew.navigator.vc;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
public interface VersionControl {

    /**
     * Determines whether a directory or file is handled by a version control
     * system.
     *
     * @param file The directory to check
     * @return if it is handled by this VCS
     */
    boolean controls(File file);

    /**
     * Diffs a file with this VC.
     *
     * @param file The file to perform the diff.
     * @return <code>true</code>, if successful.
     */
    boolean diff(File file);

    /**
     * Displays the log of a file.
     *
     * @param file The file to obtain the log of.
     * @return <code>true</code>, if successful.
     */
    boolean log(File file);

    /**
     * Returns a repository for a given file.
     *
     * @param fileInRepository The file in a working copy
     * @return repository containing details
     */
    Repository findRepository(File fileInRepository);

    /**
     * @return the name of the VC.
     */
    String getName();
}