package de.renew.tablet.commands;

import CH.ifa.draw.util.Command;

import de.renew.plugin.IPlugin;

import de.renew.tablet.TabletPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class ShowPaletteCommand extends Command {
    IPlugin _plugin;

    public ShowPaletteCommand(IPlugin plugin) {
        super("show / hide tablet tools");

        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     * @see Command#execute()
     */
    public void execute() {
        //        logger.debug ("showPaletteCommand executed.");
        TabletPlugin tp = (TabletPlugin) _plugin;
        tp.create();
    }
}