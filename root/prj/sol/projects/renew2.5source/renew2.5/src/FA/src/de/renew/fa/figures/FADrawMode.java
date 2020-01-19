/*
 * Created on May 22, 2006
 *
 */
package de.renew.fa.figures;

import de.renew.fa.FAPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;


/**
 * @author cabac
 *
 */
public class FADrawMode {
    public static final int STANDARD = 0;
    public static final int ALTERNTIVE = 1;
    private static FADrawMode instance;
    private int _mode = ALTERNTIVE;

    /**
     *
     */
    private FADrawMode() {
        super();
        IPlugin plugin = getplugin();

        //System.out.println("=================="+plugin);
        if (plugin != null) {
            String mode = plugin.getProperties().getProperty("de.renew.fa.mode");
            if ("alternative".equals(mode)) {
                _mode = ALTERNTIVE;
            } else if ("standard".equals(mode)) {
                _mode = STANDARD;
            }
        }
    }

    /**
     * @return
     */
    private IPlugin getplugin() {
        IPlugin plugin = null;
        try {
            plugin = PluginManager.getInstance()
                                  .getPluginsProviding("de.renew.fa").iterator()
                                  .next();
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return plugin;
    }

    /**
     * @return
     */
    public static FADrawMode getInstance() {
        if (instance == null) {
            instance = new FADrawMode();
        }
        return instance;
    }

    public int getMode() {
        return _mode;
    }

    public void setMode(int mode) {
        _mode = mode;
        IPlugin p = getplugin();
        if (p != null) {
            ((FAPlugin) p).switchPalette();
        }
    }
}