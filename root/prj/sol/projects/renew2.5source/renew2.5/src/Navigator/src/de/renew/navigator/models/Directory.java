package de.renew.navigator.models;

import java.util.List;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
final public class Directory extends TreeElement {

    /**
     * Flags whether this directory is opened.
     */
    protected boolean opened;

    /**
     * Holds the directory type of this tree element.
     */
    protected DirectoryType type;

    public List<TreeElement> getChildren() {
        return elements;
    }

    public void setChildren(List<TreeElement> children) {
        this.elements = children;
        setChanged();
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
        if (opened) {
            if (parent instanceof Directory) {
                ((Directory) parent).setOpened(true);
            }
        } else {
            for (TreeElement child : elements) {
                if (child instanceof Directory) {
                    ((Directory) child).setOpened(false);
                }
            }
        }

        setChanged();
    }

    public DirectoryType getType() {
        return type;
    }

    public void setType(DirectoryType type) {
        this.type = type;
    }

    @Override
    public void setExcluded(boolean excluded) {
        super.setExcluded(excluded);
        if (excluded) {
            for (TreeElement child : elements) {
                child.setExcluded(true);
            }
            return;
        }

        if (parent instanceof TreeElement) {
            ((TreeElement) parent).setExcluded(false);
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", elements=" + elements.size();
    }

    /**
     * Expands all directories recursively.
     */
    public void expandAll() {
        setOpened(true);
        for (TreeElement child : elements) {
            if (!(child instanceof Directory)) {
                continue;
            }

            Directory directory = (Directory) child;
            directory.expandAll();
        }
    }
}