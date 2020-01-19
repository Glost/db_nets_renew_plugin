package de.renew.appleui;

import com.apple.eawt.Application;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.SoftDependency;


/**
 * This plugin provides basic support of the MacOS X user interface to the
 * plugin system. It requires the Apple-specific extension package
 * <code>com.apple.eawt</code> for compilation and execution.
 * <p>
 * Included features are:
 * <ul>
 * <li> Apple "quit" events (like Command-Q) are mapped to a clean shutdown
 * of the plugin system. </li>
 * <li>
 * </ul>
 * </p>
 * Created: Thu Jul  8  2004
 * Modified: November 2012
 * Modified: July 2014
 *
 * @author Michael Duvigneau
 * @author Lawrence Cabac
 * @author Konstantin Simon Maria Möllers
 * @version 0.4.1
 */
public class AppleUI extends PluginAdapter {
    private Application app = null;
    private AppleUIListener listener = null;
    private SoftDependency guiDependency;

    /**
     * Creates a AppleUI with the given PluginProperties.
     *
     * @param props the plugin configuration.
     */
    public AppleUI(PluginProperties props) {
        super(props);

        String useScreenMenubar = props.getProperty("com.apple.macos.useScreenMenubar",
                                                    "true");
        String menuAboutName = props.getProperty("com.apple.mrj.application.apple.menu.about.name",
                                                 "Renew");

        // Setting properties
        System.setProperty("com.apple.macos.useScreenMenubar", useScreenMenubar);
        System.setProperty("apple.laf.useScreenMenuBar", useScreenMenubar);
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                           menuAboutName);
    }

    /**
     * Connects to the eawt application object.
     */
    public void init() {
        app = Application.getApplication();
        listener = new AppleUIListener();
        setAboutDisplayer(null);
        app.setAboutHandler(listener);
        app.addAppEventListener(listener);
        app.setQuitHandler(listener);
        app.setOpenFileHandler(listener);
        guiDependency = new SoftDependency(this, "de.renew.gui",
                                           "de.renew.appleui.GuiDependencyListener");
    }

    /**
     * Disconnects from the eawt application object (as far as
     * possible).
     *
     * @return true
     */
    public boolean cleanup() {
        if (app != null) {
            app.removeAppEventListener(listener);
            app = null;
            listener = null;
        }

        //NOTICEnull
        if (guiDependency != null) {
            guiDependency.discard();
            guiDependency = null;
        }
        return true;
    }

    /**
     * Configures an <code>AboutDisplayer</code>.
     *
     * @param displayer the about box displayer.
     *                  If <code>null</code>, the menu entry is disabled.
     */
    void setAboutDisplayer(AboutDisplayer displayer) {
        if ((app != null) && (listener != null)) {
            listener.setAboutDisplayer(displayer);
        }
    }
}