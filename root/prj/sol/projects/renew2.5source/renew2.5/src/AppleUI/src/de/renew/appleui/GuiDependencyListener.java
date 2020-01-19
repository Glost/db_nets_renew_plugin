package de.renew.appleui;

import de.renew.gui.GuiPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.SoftDependencyListener;


/**
 * Connects to the "Renew Gui" plugin if available.
 *
 * @author Michael Duvigneau
 **/
public class GuiDependencyListener implements SoftDependencyListener,
                                              AboutDisplayer {
    private AppleUI applePlugin;
    private GuiPlugin guiPlugin;

    /**
     * Listener for the GuiDependencies
     *
     * @param applePlugin AppleUI which should be used
     */
    public GuiDependencyListener(IPlugin applePlugin) {
        this.applePlugin = (AppleUI) applePlugin;
    }

    /**
     * Clears the about box displayer info.
     *
     * @param plugin {@inheritDoc}
     **/
    public final void serviceRemoved(final IPlugin plugin) {
        applePlugin.setAboutDisplayer(null);
        this.guiPlugin = null;
    }

    /**
     * Registers this object as about box displayer.
     * @param plugin {@inheritDoc}
     **/
    public final void serviceAvailable(final IPlugin plugin) {
        this.guiPlugin = (GuiPlugin) plugin;
        applePlugin.setAboutDisplayer(this);
    }

    public void displayAboutBox() {
        if (guiPlugin != null) {
            guiPlugin.createAboutBox().setVisible(true);
        }
    }

    public void bringMenuFrameToFront() {
        if (guiPlugin != null) {
            guiPlugin.bringMenuFrameToFront();
        }
    }
}