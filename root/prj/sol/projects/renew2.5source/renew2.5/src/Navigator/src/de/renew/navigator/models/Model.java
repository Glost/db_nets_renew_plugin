package de.renew.navigator.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-08
 */
abstract public class Model extends Observable {

    /**
     * List of all parents belonging to navigator trees.
     */
    protected List<TreeElement> elements;

    public Model() {
        this.elements = new LinkedList<TreeElement>();
    }

    /**
     * Returns whether the model contains a specific tree element.
     *
     * @param treeElement the tree element to check.
     * @return <code>true</code>, if it is contained.
     */
    public boolean contains(TreeElement treeElement) {
        return this.elements.contains(treeElement);
    }

    @Override
    protected synchronized void setChanged() {
        super.setChanged();
    }

    /**
     * Adds a tree element to the tree.
     *
     * @param element The element to add.
     */
    public void add(TreeElement element) {
        element.setParent(this);
        this.elements.add(element);
        setChanged();
    }

    /**
     * Removes a tree element from the tree.
     *
     * @param element The element to remove.
     */
    public void remove(TreeElement element) {
        element.setParent(null);
        this.elements.remove(element);
        setChanged();
    }
}