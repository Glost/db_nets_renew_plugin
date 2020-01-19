package de.renew.navigator;

import de.renew.navigator.models.NavigatorFileTree;

import javax.swing.JTree;


/**
 * An extender for the navigator plugin. Plugins and buttons can be
 * registered at the Navigator plugin.
 *
 * @author cabac
 */
public interface NavigatorExtender {

    /**
     * Registers a navigator extension as plugin.
     *
     * @param extension plugin to register
     */
    void registerExtension(NavigatorExtension extension);

    /**
     * Deregisters a navigator extension as plugin.
     *
     * @param extension plugin to deregister
     */
    boolean deregisterExtension(NavigatorExtension extension);

    /**
     * @return the model behind the navigator
     */
    NavigatorFileTree getModel();

    /**
     * @return current available file tree
     */
    JTree getTree();
}