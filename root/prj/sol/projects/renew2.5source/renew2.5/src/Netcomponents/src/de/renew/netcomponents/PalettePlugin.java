package de.renew.netcomponents;

import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.Command;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.command.CLCommand;

import de.renew.util.StringUtil;

import java.net.URL;

import java.util.Vector;


/**
 * The ComponentsPlugin serves as generic facade for net components
 * repositories provided as plugins.
 *
 * In order to define the repository directory provide the folder name
 * relative to the location of the plugin directory in plugin.cfg as property
 * and provide the name of that property to a property with key:
 * <i>de.renew.nc.dir-prop-name</i>.
 * In order to provide a initialization property, provide a similar combination
 * with the key name as value for the property:  <i>de.renew.nc.init-prop-name</i>.
 *
 *  This version is derived from the MulanComponentsPlugin (version 0.5.0)
 *
 * <pre>
 * </pre>
 * @author Lawrence Cabac
 * @version 0.1
 *
 */
public class PalettePlugin extends PluginAdapter
        implements ComponentsPluginExtender {
    private URL location;
    protected ComponentsToolPlugin _ncPlugin;

    /**
     * Key for property that provides the name of the property that provides
     * the name of the directory containing tools (repository of renew drawings).
     */
    public static final String TOOL_DIR_PROPERTY_NAME = "de.renew.nc.dir-prop-name";

    /**
     * Key for property that provides the name of the property that provides
     * the initialization state of the palette.
     */
    public static final String TOOL_INIT_PROPERTY_NAME = "de.renew.nc.init-prop-name";

    public PalettePlugin(URL location) throws PluginException {
        super(location);
        setLocation(location);
    }

    public PalettePlugin(PluginProperties props) {
        super(props);
        setLocation(props.getURL());
    }

    /**
     * @see de.renew.plugin.IPlugin#init()
     */
    public void init() {
        if (getToolDirPath() == null) {
            logger.warn(PalettePlugin.class.getSimpleName() + ": "
                        + getToolDirPath()
                        + " not set in plugin.cfg of plugin " + getName() + ".");
        }

        _ncPlugin = ComponentsToolPlugin.getCurrent();
        _ncPlugin.registerPlugin(this);

        String toolInitPropertyName = getProperties()
                                          .getProperty(TOOL_INIT_PROPERTY_NAME);

        boolean init = getProperties().getBoolProperty(toolInitPropertyName);
        if (init) {
            _ncPlugin.createPalette(getToolDirPath(), getName(), this);
        }
    }

    public boolean cleanup() {
        _ncPlugin.removePalette(getToolDirPath());
        _ncPlugin.deregisterPlugin(this);
        return true;
    }

    public URL getLocation() {
        return location;
    }

    public void setLocation(URL url) {
        location = url;
    }

    /* (non-Javadoc)
     * @see de.renew.netcomponents.IComponentsPluginExtender#getMenuCommands()
     */
    public Vector<Command> getMenuCommands() {
        Vector<Command> v = new Vector<Command>();
        String tooldirpath = getToolDirPath();
        v.add(new ShowNetComponentsToolCommand(_ncPlugin,
                                               getName() + " " + getVersion(),
                                               tooldirpath));
        return v;
    }

    public String getToolDirPath() {
        String tooldirpath = StringUtil.getPath(getLocation().getPath()) + "/"
                             + getProperties()
                                   .getProperty(getProperties()
                                                    .getProperty(TOOL_DIR_PROPERTY_NAME));
        return tooldirpath;
    }

    /* (non-Javadoc)
     * @see de.renew.netcomponents.IComponentsPluginExtender#getPromptCommand()
     */
    public Vector<CLCommand> getPromptCommands() {
        // no commands added
        return null;
    }

    /* (non-Javadoc)
     * @see de.renew.netcomponents.ComponentsPluginExtender#getAdditionalButtons()
     */
    public Vector<ToolButton> getAdditionalButtons() {
        // no toolbuttons added
        return null;
    }
}