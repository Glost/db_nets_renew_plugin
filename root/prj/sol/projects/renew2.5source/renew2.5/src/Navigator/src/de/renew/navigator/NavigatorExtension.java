package de.renew.navigator;

import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;


/**
 * An extension to the Navigator plugin.
 *
 * @author cabac
 */
public interface NavigatorExtension {

    /**
     * Configures the navigator with additional features.
     *
     * @param config The configuration to apply.
     */
    void configure(final NavigatorConfigurator config);

    /**
     * Gets a menu item.
     */
    JMenuItem getMenuItem(JTree tree, int x, int y, Object lastPathComponent,
                          MutableTreeNode mtn);
}