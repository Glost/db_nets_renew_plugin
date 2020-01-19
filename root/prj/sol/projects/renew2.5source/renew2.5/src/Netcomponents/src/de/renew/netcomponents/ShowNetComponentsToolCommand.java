package de.renew.netcomponents;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class ShowNetComponentsToolCommand extends Command {
    IPlugin _plugin;
    private String _toolsDirPropertyName;
    private String _menutext;
    private ComponentsPluginExtender _pluginPlugin;

    public ShowNetComponentsToolCommand(IPlugin plugin, String menutext,
                                        String toolsDir) {
        super(menutext);
        _plugin = plugin;
        _toolsDirPropertyName = toolsDir;
        _menutext = menutext;
        _pluginPlugin = null;

    }

    public ShowNetComponentsToolCommand(IPlugin plugin, String menutext,
                                        String toolsDir,
                                        ComponentsPluginExtender pluginPlugin) {
        super(menutext);
        _plugin = plugin;
        _toolsDirPropertyName = toolsDir;
        _menutext = menutext;
        _pluginPlugin = pluginPlugin;

    }

    /**
     * Shows the Net Components Tools Palette
     *
     * @see Command#execute()
     */
    public void execute() {
        ComponentsToolPlugin ctp = (ComponentsToolPlugin) _plugin;
        ctp.createPalette(_toolsDirPropertyName, _menutext, _pluginPlugin);
    }
}