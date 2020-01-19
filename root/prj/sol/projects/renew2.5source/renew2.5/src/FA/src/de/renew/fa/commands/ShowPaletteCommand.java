package de.renew.fa.commands;

import CH.ifa.draw.util.Command;

import de.renew.fa.FAPlugin;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class ShowPaletteCommand extends Command {
    IPlugin _plugin;

    public ShowPaletteCommand(IPlugin plugin) {
        super("FA Drawing Tool");

        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     *
     * @see Command#execute()
     */
    @Override
    public void execute() {
        //        logger.debug ("showPaletteCommand executed.");
        FAPlugin pcp = (FAPlugin) _plugin;
        pcp.create();
    }
}