package de.renew.netcomponents;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for removing the last loaded palette.
 *
 * @author Lawrence Cabac
 */
public class RemoveLastNetComponentsToolCommand extends Command {
    private IPlugin _plugin;

    public RemoveLastNetComponentsToolCommand(IPlugin plugin) {
        super("remove last");
        _plugin = plugin;
    }

    /**
     * Initiates the removal of the Net-Component-Tool Palette which is last in the list.
     * @see Command#execute()
     */
    public void execute() {
        ComponentsToolPlugin ctp = (ComponentsToolPlugin) _plugin;
        ctp.removeLastPalette();
    }
}