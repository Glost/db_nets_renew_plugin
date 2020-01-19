package de.renew.diagram;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class ShowPaletteCommand extends Command {
    IPlugin _plugin;

    public ShowPaletteCommand(IPlugin plugin) {
        super("show / hide");

        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     * @see Command#execute()
     */
    public void execute() {
        //        logger.debug ("showPaletteCommand executed.");
        PaletteCreatorPlugin pcp = (PaletteCreatorPlugin) _plugin;
        pcp.create();
    }
}