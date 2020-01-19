package de.renew.navigator.models;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
abstract public class TreeElement extends Model {

    /**
     * The parent tree element.
     */
    protected Model parent;

    /**
     * The name to display for this tree element.
     */
    protected String name;

    /**
     * Whether this tree element is excluded from the view.
     */
    protected boolean excluded;

    /**
     * Which file is represented by this tree element.
     */
    protected File file;

    /**
     * Propagates the change of this model element.
     */
    protected synchronized void setChanged() {
        super.setChanged();

        if (parent == null) {
            return;
        }

        parent.setChanged();
    }

    public Model getParent() {
        return parent;
    }

    public void setParent(Model parent) {
        this.parent = parent;
        setChanged();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setChanged();
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
        setChanged();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        setChanged();
    }

    public void expandAll() {
    }

    @Override
    public String toString() {
        return "TreeElement{" + "name='" + name + '\'' + ", file=" + file + '}';
    }
}