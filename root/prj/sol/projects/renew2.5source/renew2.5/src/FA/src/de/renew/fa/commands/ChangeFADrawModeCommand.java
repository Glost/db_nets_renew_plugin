package de.renew.fa.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Command;

import de.renew.fa.figures.FADrawMode;

import de.renew.plugin.IPlugin;


/**
 * The command that is executed for showing the netcomponents.
 *
 * @author Lawrence Cabac
 */
public class ChangeFADrawModeCommand extends Command {
    IPlugin _plugin;

    public ChangeFADrawModeCommand(IPlugin plugin) {
        super("FA toggle mode");

        _plugin = plugin;
    }

    /**
     * Shows the Net Components Tools Palette
     *
     * @see Command#execute()
     */
    @Override
    public void execute() {
        FADrawMode mode = FADrawMode.getInstance();
        mode.setMode(1 - mode.getMode());
        DrawingEditor editor = DrawPlugin.getCurrent().getDrawingEditor();
        FigureEnumeration en = editor.drawing().figures();
        while (en.hasMoreElements()) {
            Figure fig = en.nextFigure();
            //System.out.println("========= figure "+fig);
            fig.changed();
        }
        editor.view().checkDamage();
    }
}