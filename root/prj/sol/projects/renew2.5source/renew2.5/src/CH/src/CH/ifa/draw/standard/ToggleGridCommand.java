/*
 * @(#)ToggleGridCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.PointConstrainer;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.GUIProperties;

import de.renew.plugin.PluginProperties;

import java.awt.Point;

import java.util.Properties;


/**
 * A command to toggle the snap to grid behavior.
 */
public class ToggleGridCommand extends Command {
    // protected DrawingEditor fEditor;
    private Point fGrid;

    protected DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    /**
     * Constructs a toggle grid command.
     * @param name the command name
     * @param grid the grid size. A grid size of 1,1 turns grid snapping off.
     */
    public ToggleGridCommand(String name) {
        super(name);
        // fEditor = editor;
    }

    public boolean isExecutable() {
        if (getEditor() == NullDrawingEditor.INSTANCE) {
            return false;
        }
        return super.isExecutable();
    }

    public void execute() {
        DrawingView view = getEditor().view();
        PointConstrainer grid = view.getConstrainer();
        if (grid != null) {
            view.setConstrainer(null);
        } else {
            int size;
            DrawPlugin current = DrawPlugin.getCurrent();
            if (current == null) {
                Properties properties = GUIProperties.getProperties();
                size = ((PluginProperties) properties).getIntProperty(DrawPlugin.CH_IFA_DRAW_GRID_SIZE,
                                                                      5);
            } else {
                size = current.getProperties()
                              .getIntProperty(DrawPlugin.CH_IFA_DRAW_GRID_SIZE,
                                              5);
            }
            fGrid = new Point(size, size); // let's have only square grids.
            view.setConstrainer(new GridConstrainer(fGrid.x, fGrid.y));
        }
    }
}