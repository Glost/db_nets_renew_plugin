package de.renew.navigator.gui;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorExtension;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.NavigatorPlugin;
import de.renew.navigator.models.TreeElement;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
public class NavigatorGuiProxy implements NavigatorGui {
    private final FilesystemController filesystem;
    private final NavigatorPlugin plugin;
    private NavigatorGuiImpl proxy;
    private final LinkedList<NavigatorExtension> tempExtensions;

    public NavigatorGuiProxy(FilesystemController filesystem,
                             NavigatorPlugin plugin) {
        this.filesystem = filesystem;
        this.plugin = plugin;
        tempExtensions = new LinkedList<NavigatorExtension>();
    }

    @Override
    public void openWindow() {
        ensureProxyInitialized().openWindow();
    }

    @Override
    public void closeWindow() {
        ensureProxyInitialized().closeWindow();

        ensureProxyFreed();
    }

    @Override
    public List<NavigatorExtension> getExtensions() {
        if (proxy == null) {
            return tempExtensions;
        }

        return proxy.getExtensions();
    }

    @Override
    public void addExtension(NavigatorExtension extension) {
        if (proxy == null) {
            tempExtensions.add(extension);
            return;
        }

        proxy.addExtension(extension);
    }

    @Override
    public boolean removeExtension(NavigatorExtension extension) {
        if (proxy == null) {
            return tempExtensions.remove(extension);
        }

        return proxy.removeExtension(extension);
    }

    @Override
    public DefaultFileTreeCellRenderer getTreeCellRenderer() {
        return ensureProxyInitialized().getTreeCellRenderer();
    }

    @Override
    public JTree getTree() {
        return ensureProxyInitialized().getTree();
    }

    @Override
    public DefaultMutableTreeNode getRootNode() {
        return ensureProxyInitialized().getRootNode();
    }

    @Override
    public void collapseAll() {
        ensureProxyInitialized().collapseAll();
    }

    @Override
    public void expand() {
        ensureProxyInitialized().expand();
    }

    @Override
    public void removeSelectedNodes() {
        ensureProxyInitialized().removeSelectedNodes();
    }

    @Override
    public List<TreeElement> getSelectedElements() {
        return ensureProxyInitialized().getSelectedElements();
    }

    private NavigatorGuiImpl ensureProxyInitialized() {
        if (proxy != null) {
            return proxy;
        }

        proxy = new NavigatorGuiImpl(filesystem, plugin);

        // Add all temporary extensions to the GUI.
        for (NavigatorExtension extension : tempExtensions) {
            proxy.addExtension(extension);
        }
        tempExtensions.clear();
        return proxy;
    }

    private void ensureProxyFreed() {
        if (proxy == null) {
            return;
        }

        for (NavigatorExtension extension : proxy.getExtensions()) {
            tempExtensions.add(extension);
        }

        proxy.dispose();
        proxy = null;
    }
}