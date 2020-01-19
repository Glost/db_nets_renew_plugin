package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginProperties;


/**
 * This command displays a status line message with the version of its
 * associated plugin.
 *
 * @author Lawrence Cabac
 */
public class VersionInfoCommand extends Command {
    IPlugin _plugin;

    /**
     * Creates an <code>VersionInfoCommand</code> for the given plugin.
     * @param plugin the plugin instance whose version is of interest.
     */
    public VersionInfoCommand(IPlugin plugin) {
        super("About  v " + ((PluginAdapter) plugin).getVersion());

        _plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() {
        DrawPlugin current = DrawPlugin.getCurrent();
        current.showStatus(((PluginAdapter) _plugin).getLongVersion());
        System.out.println("\n");
        System.out.println(((PluginAdapter) _plugin).getLongVersion());
        PluginProperties props = ((PluginAdapter) _plugin).getProperties();
        System.out.println("Compilation date is: "
                           + props.getFilteredProperty("compile.date"));
        System.out.println("\n");
    }
}