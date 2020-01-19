package de.renew.diagram;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class GeneratePeersCommand extends Command {
    IPlugin _plugin;

    public GeneratePeersCommand(IPlugin plugin) {
        super("Generate Peers");

        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     * @see Command#execute()
     */
    public void execute() {
        //        logger.debug ("showPaletteCommand executed.");
        ((PaletteCreatorPlugin) _plugin).generatePeers();
    }
}