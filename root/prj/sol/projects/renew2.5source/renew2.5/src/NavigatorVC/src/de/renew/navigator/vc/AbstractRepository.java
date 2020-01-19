package de.renew.navigator.vc;

import de.renew.util.StringUtil;

import java.io.File;

import java.net.URI;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-19
 */
public abstract class AbstractRepository implements Repository {

    /**
     * The work tree of this repository.
     */
    protected final File rootDirectory;

    /**
     * The version control handling this repository.
     */
    protected final VersionControl versionControl;

    /**
     * The last authored commit on this repository.
     */
    protected Commit lastCommit;
    protected final Set<File> ignored;
    protected final Set<File> added;
    protected final Set<File> modified;

    public AbstractRepository(File rootDirectory, VersionControl versionControl) {
        this.rootDirectory = rootDirectory;
        this.versionControl = versionControl;
        lastCommit = null;
        ignored = new HashSet<File>();
        added = new HashSet<File>();
        modified = new HashSet<File>();
    }

    final public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    final public VersionControl getVersionControl() {
        return versionControl;
    }

    @Override
    public Commit getLastCommit() {
        return lastCommit;
    }

    @Override
    final public URI makeRelativeURI(File fileInRepository) {
        return StringUtil.makeRelative(rootDirectory.toURI(),
                                       fileInRepository.toURI());
    }

    @Override
    public String toString() {
        return "[" + rootDirectory.toString() + "] " + versionControl.getName()
               + "@" + getBranch();
    }

    @Override
    final public Set<File> getIgnored() {
        return ignored;
    }

    @Override
    final public Set<File> getAdded() {
        return added;
    }

    @Override
    final public Set<File> getModified() {
        return modified;
    }

    /**
     * @return the last commit.
     */
    protected abstract Commit retrieveLastCommit();
}