package de.renew.pd;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.util.CommandMenu;

import de.renew.pd.commands.CreateApplicationStructureCommand;
import de.renew.pd.generating.LibraryPluginGenerator;
import de.renew.pd.generating.StandardPluginGenerator;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.command.CLCommand;

import java.net.URL;

import java.util.Vector;


/**
 * Facade for the PluginDevelopment Plugin for Renew.
 *
 *<pre>
 * 0.1 Adapted from UseCaseComponents
 * 0.2 Fixed prompting for sources
 *</pre>
 * @author Lawrence Cabac
 * @version 0.2
 *
 */
public class PluginDevelopmentPlugin extends PluginAdapter {

    /**
     * The toolsdir property name.
     */


    //protected static String TOOLDIRPROPERTY = "de.renew.nc.usecase.dir";
    static private URL location;

    public PluginDevelopmentPlugin(URL location) throws PluginException {
        super(location);

        setLocation(location);


    }

    public PluginDevelopmentPlugin(PluginProperties props) {
        super(props);

        setLocation(props.getURL());
    }

    /**
     * @see de.renew.plugin.IPlugin#init()
     */
    public void init() {
        DrawPlugin current = DrawPlugin.getCurrent();
        if (current == null) {
            return;
        }
        MenuManager mm = current.getMenuManager();
        CommandMenu _menu = new CommandMenu("Plugin Development");
        _menu.putClientProperty(MenuManager.ID_PROPERTY, "de.renew.pd");
        _menu.add(new CreateApplicationStructureCommand("Create Renew Plugin Folder",
                                                        new StandardPluginGenerator()));
        _menu.add(new CreateApplicationStructureCommand("Create Renew Library Plugin Folder",
                                                        new LibraryPluginGenerator()));
        mm.registerMenu(DrawPlugin.PLUGINS_MENU, _menu);
    }

    public boolean cleanup() {
        return true;
    }

    static public URL getLocation() {
        return location;
    }

    static public void setLocation(URL url) {
        location = url;
    }

    /* (non-Javadoc)
     * @see de.renew.netcomponents.IComponentsPluginExtender#getPromptCommand()
     */
    public Vector<CLCommand> getPromptCommands() {
        return null;
    }
}