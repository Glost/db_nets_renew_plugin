package de.renew.navigator.vc;

import java.io.File;

import java.net.URI;

import java.util.Set;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public interface Repository {

    /**
     * @return the work tree of the repository.
     */
    File getRootDirectory();

    /**
     * @return the version control managing this repository.
     */
    VersionControl getVersionControl();

    /**
     * @return the modified files.
     */
    Set<File> getModified();

    /**
     * @return the added files.
     */
    Set<File> getAdded();

    /**
     * @return the ignored files.
     */
    Set<File> getIgnored();

    /**
     * @return the branch this repository is in.
     */
    String getBranch();

    /**
     * @return the URL on the remote end.
     */
    String getRemoteURL();

    /**
     * @return the last authored commit.
     */
    Commit getLastCommit();

    /**
     * @param fileInRepository a file in the repository to make the URI for.
     * @return a realtive URi for the given file.
     */
    URI makeRelativeURI(File fileInRepository);

    /**
     * Updates the repository state.
     */
    void update();
}