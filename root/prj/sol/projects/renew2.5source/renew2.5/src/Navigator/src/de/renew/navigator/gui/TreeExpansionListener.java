package de.renew.navigator.gui;

import de.renew.navigator.events.DirectoryOpeningEvent;
import de.renew.navigator.models.Directory;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.TreeElement;

import javax.swing.event.TreeExpansionEvent;


/**
 * {@link javax.swing.event.TreeExpansionListener} to handle collapsing and expansion of tree nodes
 *
 * @author Eva Mueller (3emuelle)
 * @version Jan, 2014
 */
class TreeExpansionListener implements javax.swing.event.TreeExpansionListener {
    private static boolean active = true;
    private final NavigatorFileTree model;

    public TreeExpansionListener(NavigatorFileTree model) {
        this.model = model;
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        TreeExpansionListener.active = active;
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        if (!active) {
            return;
        }

        final FileTreeNode node = extractFileTreeNode(event);
        extractDirectory(node).setOpened(false);

        model.notifyObservers(new DirectoryOpeningEvent(false, node));
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        if (!active) {
            return;
        }

        final FileTreeNode node = extractFileTreeNode(event);
        extractDirectory(node).setOpened(true);

        model.notifyObservers(new DirectoryOpeningEvent(true, node));
    }

    private FileTreeNode extractFileTreeNode(TreeExpansionEvent event) {
        final Object lastPathComponent = event.getPath().getLastPathComponent();

        if (!(lastPathComponent instanceof FileTreeNode)) {
            throw new IllegalArgumentException("Last path component has to be a FileTreeNode");
        }

        return (FileTreeNode) lastPathComponent;
    }

    private Directory extractDirectory(FileTreeNode node) {
        final TreeElement model = node.getModel();

        if (model instanceof Directory) {
            return (Directory) model;
        }

        return (Directory) model.getParent();
    }
}