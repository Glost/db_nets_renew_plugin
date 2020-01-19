package de.renew.netcomponents;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class SelectNetComponentsToolCommand extends Command {
    IPlugin _plugin;

    public SelectNetComponentsToolCommand(IPlugin plugin) {
        super("select from directory");
        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     * @see Command#execute()
     */
    public void execute() {
        ComponentsToolPlugin ctp = (ComponentsToolPlugin) _plugin;
        ctp.selectDirAndCreatePalette();
    }
}