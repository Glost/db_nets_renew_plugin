package de.renew.navigator.io;

import de.renew.navigator.models.TreeElement;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-07
 */
public interface IOLoader {

    /**
     * Loads a path into a model structure.
     *
     * @param path The path specification.
     * @param progressListener The listener to inform meanwhile.
     * @return Model object containing path information.
     */
    TreeElement loadPath(File path, ProgressListener progressListener);

    /**
     * Refreshes a model for a given path.
     *  @param model The model to refresh.
     * @param path The path specification.
     * @param progressListener The listener to inform meanwhile.
     */
    void refreshPath(TreeElement model, File path,
                     ProgressListener progressListener);
}