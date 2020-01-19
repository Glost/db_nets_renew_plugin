/*
 * @(#)SnapToGridCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.PointConstrainer;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.util.GUIProperties;

import de.renew.plugin.PluginProperties;

import java.awt.Point;

import java.util.Properties;


/**
 * A command to toggle the snap to grid behavior.
 */
public class SnapToGridCommand extends UndoableCommand {
    // protected DrawingEditor getEditor();
    private Point fGrid;

    /**
     * Constructs a snap to grid command.
     * @param name the command name
     * @param grid the grid size. A grid size of 1,1 turns grid snapping off.
     */
    public SnapToGridCommand(String name) {
        super(name);
        //getEditor() = editor;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        PointConstrainer grid = view.getConstrainer();
        if (grid == null) {
            int size;
            DrawPlugin current = DrawPlugin.getCurrent();
            if (current == null) {
                Properties properties = GUIProperties.getProperties();
                size = ((PluginProperties) properties).getIntProperty(DrawPlugin.CH_IFA_DRAW_GRID_SIZE,
                                                                      5);
            } else {
                current.showStatus("No active grid - using default grid.");
                size = current.getProperties()
                              .getIntProperty(DrawPlugin.CH_IFA_DRAW_GRID_SIZE,
                                              5);
            }
            fGrid = new Point(size, size);
            grid = new GridConstrainer(fGrid.x, fGrid.y);
        }

        FigureEnumeration selection = view.selectionElements();

        while (selection.hasMoreElements()) {
            Figure f = selection.nextFigure();
            Point c = f.center();
            Point cc = grid.constrainPoint(f.center());
            f.moveBy(cc.x - c.x, cc.y - c.y);
        }
        view.checkDamage();
        return true;
    }
}