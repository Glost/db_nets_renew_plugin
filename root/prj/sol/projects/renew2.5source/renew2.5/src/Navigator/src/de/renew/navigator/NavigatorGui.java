package de.renew.navigator;

import de.renew.navigator.gui.FileTreeCellRenderer;
import de.renew.navigator.models.TreeElement;

import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
public interface NavigatorGui {

    /**
     * Opens the Navigator window.
     */
    void openWindow();

    /**
     * Closes the Navigator window.
     */
    void closeWindow();

    /**
     * Adds an extension to the GUI.
     *
     * @param extension The extension to add
     */
    void addExtension(NavigatorExtension extension);

    /**
     * Removes an extension from the GUI.
     *
     * @param extension The extension to remove
     */
    boolean removeExtension(NavigatorExtension extension);

    /**
     * Returns all activated Extensions.
     */
    List<NavigatorExtension> getExtensions();

    FileTreeCellRenderer getTreeCellRenderer();

    JTree getTree();

    DefaultMutableTreeNode getRootNode();

    void collapseAll();

    void expand();

    void removeSelectedNodes();

    /**
     * Returns all selected tree elements.
     */
    List<TreeElement> getSelectedElements();
}