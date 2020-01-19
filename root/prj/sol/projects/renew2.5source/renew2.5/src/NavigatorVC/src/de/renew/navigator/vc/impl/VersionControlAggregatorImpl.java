package de.renew.navigator.vc.impl;

import org.apache.log4j.Logger;

import de.renew.navigator.vc.Repository;
import de.renew.navigator.vc.VersionControl;
import de.renew.navigator.vc.VersionControlAggregator;

import java.io.File;

import java.util.LinkedList;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
public class VersionControlAggregatorImpl implements VersionControlAggregator {

    /**
     * Log4j logger instance.
     */
    public static final Logger logger = Logger.getLogger(VersionControlAggregatorImpl.class);

    /**
     * List of version controls used by this aggregator.
     */
    private final LinkedList<VersionControl> versionControls;

    public VersionControlAggregatorImpl() {
        versionControls = new LinkedList<VersionControl>();
    }

    @Override
    public void addVersionControl(VersionControl vcs) {
        versionControls.add(vcs);
    }

    @Override
    public boolean removeVersionControl(VersionControl vcs) {
        return versionControls.remove(vcs);
    }

    @Override
    public boolean controls(File workingDir) {
        for (VersionControl versionControl : versionControls) {
            if (versionControl.controls(workingDir)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean diff(File file) {
        final Repository r = findRepository(file);
        if (r == null) {
            warnNotInARepository(file);
            return false;
        }

        return r.getVersionControl().diff(file);
    }

    @Override
    public boolean log(File file) {
        final Repository r = findRepository(file);
        if (r == null) {
            warnNotInARepository(file);
            return false;
        }

        return r.getVersionControl().log(file);
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder("[");
        boolean isFirst = true;
        for (VersionControl versionControl : versionControls) {
            if (!isFirst) {
                builder.append(',');
            } else {
                isFirst = false;
            }
            builder.append(versionControl.getName());
        }
        builder.append(']');

        return builder.toString();
    }

    @Override
    public Repository findRepository(File fileInRepository) {
        for (VersionControl accessor : versionControls) {
            if (accessor.controls(fileInRepository)) {
                return accessor.findRepository(fileInRepository);
            }
        }

        warnNotInARepository(fileInRepository);
        return null;
    }

    private void warnNotInARepository(File file) {
        logger.warn("The file " + file.getAbsolutePath()
                    + " seems not to be in a repository known to " + getName());
    }
}