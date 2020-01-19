package de.renew.navigator.vc;

import org.apache.log4j.Logger;

import java.io.File;

import java.util.HashMap;


/**
 * The abstract version control saves the used repository for a file in memory.
 *
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-19
 */
public abstract class AbstractVersionControl implements VersionControl {

    /**
     * Log4j logger instance.
     */
    public static final Logger logger = Logger.getLogger(AbstractVersionControl.class);
    private static final HashMap<File, Repository> REPOSITORIES;

    static {
        REPOSITORIES = new HashMap<File, Repository>();
    }

    @Override
    final public boolean controls(File fileInRepository) {
        return REPOSITORIES.containsKey(fileInRepository)
               || findRepository(fileInRepository) != null;
    }

    @Override
    final public Repository findRepository(File fileInRepository) {
        // Load cached variant.
        if (REPOSITORIES.containsKey(fileInRepository)) {
            return REPOSITORIES.get(fileInRepository);
        }

        final Repository repository = buildRepository(fileInRepository);

        if (repository == null) {
            logger.warn(fileInRepository + " belongs not to " + getName());
            return null;
        }

        // Cache the repository.
        REPOSITORIES.put(fileInRepository, repository);
        REPOSITORIES.put(repository.getRootDirectory(), repository);
        logger.debug("Found " + fileInRepository + ", belongs to " + getName());

        return repository;
    }

    @Override
    public String getName() {
        final String simpleName = getClass().getSimpleName();

        final int index = simpleName.indexOf("VersionControl");
        return index == -1 ? simpleName : simpleName.substring(0, index);
    }

    abstract protected Repository buildRepository(File fileInRepository);


    /**
     * Displays an error for a given exception.
     *
     * @param exception the exception to display.
     */
    protected void errorException(Exception exception, String action) {
        logger.error("Error while " + action, exception);
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + ": " + exception);
        }
    }
}