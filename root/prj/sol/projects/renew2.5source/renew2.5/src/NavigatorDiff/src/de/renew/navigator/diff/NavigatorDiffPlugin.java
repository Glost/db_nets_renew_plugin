package de.renew.navigator.diff;

import de.renew.navigator.NavigatorConfigurator;
import de.renew.navigator.NavigatorExtension;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.NavigatorPlugin;

import de.renew.plugin.annotations.Inject;
import de.renew.plugin.annotations.Provides;
import de.renew.plugin.di.DIPlugin;

import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public class NavigatorDiffPlugin extends DIPlugin implements NavigatorExtension {
    private final FileDiffer fileDiffer;
    private final FileDiffAction fileDiffAction;
    private final NavigatorGui gui;

    @Inject
    public NavigatorDiffPlugin(NavigatorPlugin plugin, NavigatorGui gui) {
        this.gui = gui;
        fileDiffer = new FileDifferImpl();
        fileDiffAction = new FileDiffAction(plugin, fileDiffer);

        gui.addExtension(this);
    }

    @Override
    public boolean cleanup() {
        return gui.removeExtension(this);
    }

    @Override
    public void configure(NavigatorConfigurator config) {
        config.addMenuAction(fileDiffAction);
    }

    @Override
    public JMenuItem getMenuItem(JTree tree, int x, int y,
                                 Object lastPathComponent, MutableTreeNode mtn) {
        return null;
    }

    @Provides
    public FileDiffer getFileDiffer() {
        return fileDiffer;
    }
}