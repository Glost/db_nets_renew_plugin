package de.renew.console;

import de.renew.plugin.IPlugin;
import de.renew.plugin.SoftDependencyListener;


public class CHDependencyListener implements SoftDependencyListener {
    public static boolean drawPluginAvailable = false;

    public CHDependencyListener(IPlugin consolePlugin) {
    }

    @Override
    public void serviceAvailable(IPlugin plugin) {
        CHDependencyListener.drawPluginAvailable = true;
    }

    @Override
    public void serviceRemoved(IPlugin plugin) {
        CHDependencyListener.drawPluginAvailable = false;
    }
}