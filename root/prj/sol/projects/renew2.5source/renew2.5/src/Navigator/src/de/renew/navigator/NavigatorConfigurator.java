package de.renew.navigator;

import de.renew.navigator.gui.FileTreeCellRenderer;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-09-25
 */
public interface NavigatorConfigurator {

    /**
     * Registers a menu action in the navigator.
     *
     * @param action action of button to register
     */
    void addMenuAction(NavigatorAction action);

    /**
     * Registers a filter action in the navigator.
     *
     * @param action action of button to register
     */
    void addFilterAction(NavigatorAction action);

    /**
     * Adds a file tree cell renderer to the navigator.
     *
     * @param renderer the renderer to add
     */
    void addFileTreeCellRenderer(FileTreeCellRenderer renderer);
}