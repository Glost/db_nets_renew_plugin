package de.renew.navigator.events;

import de.renew.navigator.gui.FileTreeNode;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-26
 */
public class DirectoryOpeningEvent {
    private final boolean opening;
    private final FileTreeNode node;

    public DirectoryOpeningEvent(boolean opening, FileTreeNode node) {
        this.opening = opening;
        this.node = node;
    }

    /**
     * @return is the directory opening or closing?
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * @return node that represents the directory
     */
    public FileTreeNode getNode() {
        return node;
    }
}