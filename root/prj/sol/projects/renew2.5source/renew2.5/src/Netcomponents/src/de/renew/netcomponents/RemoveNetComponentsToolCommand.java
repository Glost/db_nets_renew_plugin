package de.renew.netcomponents;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for removing the netcomponents.
 * The user gets the option to select from a list of current palettes.
 *
 * @author Lawrence Cabac
 */
public class RemoveNetComponentsToolCommand extends Command {
    IPlugin _plugin;

    public RemoveNetComponentsToolCommand(IPlugin plugin) {
        super("remove pallette(s)");
        _plugin = plugin;
    }

    /**
     * Initiates the dialog for selecting Net-Component-Tools Palette for removal.
     * @see Command#execute()
     */
    public void execute() {
        ComponentsToolPlugin ctp = (ComponentsToolPlugin) _plugin;
        ctp.removePalette();
    }
}